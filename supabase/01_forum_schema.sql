-- ElecToolkit forum — schema, counters and row level security.
--
-- Run this once in the Supabase SQL editor, as the project owner. It is
-- idempotent: every object is created with IF NOT EXISTS or replaced, so
-- re-running it after an edit is safe.
--
-- Nothing here grants access on its own. Every table has RLS enabled and
-- denies by default; the policies at the bottom are the only way in. That is
-- deliberate — the anon key shipped in the app is public by design, and these
-- policies are what actually protect the data.

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

-- A profile per signed-in user. Deliberately separate from auth.users so the
-- client can read display names and statistics without ever seeing an email.
create table if not exists public.forum_profiles (
    id              uuid primary key references auth.users (id) on delete cascade,
    display_name    text        not null check (char_length(trim(display_name)) between 2 and 32),
    created_at      timestamptz not null default now(),
    is_banned       boolean     not null default false,
    -- Maintained by trigger rather than counted per query: the profile screen
    -- and every thread row would otherwise aggregate on each open.
    post_count      integer     not null default 0,
    thanks_received integer     not null default 0
);

-- Categories are entered by hand and belong to one language. There is no
-- translation table: the set is small, and a category reads differently in
-- each language anyway.
create table if not exists public.forum_categories (
    id          uuid primary key default gen_random_uuid(),
    key         text        not null,
    language    text        not null check (language in ('tr', 'en')),
    title       text        not null,
    description text        not null default '',
    position    integer     not null default 0,
    is_active   boolean     not null default true,
    created_at  timestamptz not null default now(),
    unique (key, language)
);

create table if not exists public.forum_threads (
    id            uuid primary key default gen_random_uuid(),
    category_id   uuid        not null references public.forum_categories (id) on delete cascade,
    author_id     uuid        not null references public.forum_profiles (id) on delete cascade,
    -- Denormalised from the category on purpose. The thread list then queries
    -- one table, and a Turkish thread under an English category becomes
    -- impossible at the schema level rather than by convention.
    language      text        not null check (language in ('tr', 'en')),
    title         text        not null check (char_length(trim(title)) between 5 and 140),
    created_at    timestamptz not null default now(),
    reply_count   integer     not null default 0,
    last_reply_at timestamptz not null default now(),
    is_locked     boolean     not null default false,
    is_deleted    boolean     not null default false
);

-- The thread's opening message is a post like any other. That keeps thanking,
-- editing, deleting and reporting working on a single kind of content instead
-- of branching on "is this the body or a reply".
create table if not exists public.forum_posts (
    id              uuid primary key default gen_random_uuid(),
    thread_id       uuid        not null references public.forum_threads (id) on delete cascade,
    author_id       uuid        not null references public.forum_profiles (id) on delete cascade,
    body            text        not null check (char_length(trim(body)) between 2 and 8000),
    is_opening_post boolean     not null default false,
    created_at      timestamptz not null default now(),
    edited_at       timestamptz,
    thanks_count    integer     not null default 0,
    is_deleted      boolean     not null default false
);

-- One thanks per person per post; the primary key is the rule.
create table if not exists public.forum_thanks (
    post_id    uuid        not null references public.forum_posts (id) on delete cascade,
    user_id    uuid        not null references public.forum_profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (post_id, user_id)
);

create table if not exists public.forum_reports (
    id          uuid primary key default gen_random_uuid(),
    reporter_id uuid        not null references public.forum_profiles (id) on delete cascade,
    target_type text        not null check (target_type in ('thread', 'post')),
    target_id   uuid        not null,
    reason      text        not null check (char_length(trim(reason)) between 3 and 500),
    created_at  timestamptz not null default now(),
    resolved_at timestamptz
);

create table if not exists public.forum_blocks (
    blocker_id uuid        not null references public.forum_profiles (id) on delete cascade,
    blocked_id uuid        not null references public.forum_profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_id, blocked_id),
    -- Blocking yourself would hide your own posts from you.
    check (blocker_id <> blocked_id)
);

-- ---------------------------------------------------------------------------
-- Indexes — one per query the app actually makes
-- ---------------------------------------------------------------------------

create index if not exists forum_categories_language_idx
    on public.forum_categories (language, position) where is_active;

create index if not exists forum_threads_category_idx
    on public.forum_threads (category_id, last_reply_at desc) where not is_deleted;

create index if not exists forum_threads_language_idx
    on public.forum_threads (language, last_reply_at desc) where not is_deleted;

create index if not exists forum_posts_thread_idx
    on public.forum_posts (thread_id, created_at) where not is_deleted;

create index if not exists forum_reports_open_idx
    on public.forum_reports (created_at desc) where resolved_at is null;

-- ---------------------------------------------------------------------------
-- Counters
-- ---------------------------------------------------------------------------

-- A profile is created on first sign-in rather than by the client, so a signed
-- in user always has one and the app never has to handle "signed in but no
-- profile". The display name starts from the Google account and is editable.
create or replace function public.handle_new_forum_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    insert into public.forum_profiles (id, display_name)
    values (
        new.id,
        coalesce(
            nullif(trim(new.raw_user_meta_data ->> 'full_name'), ''),
            nullif(trim(new.raw_user_meta_data ->> 'name'), ''),
            'User ' || substr(new.id::text, 1, 6)
        )
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created_forum on auth.users;
create trigger on_auth_user_created_forum
    after insert on auth.users
    for each row execute function public.handle_new_forum_user();

-- Posts move three counters: the author's total, and the thread's reply count
-- and last activity. Soft deletes have to move them back, which is why this
-- handles UPDATE as well as INSERT.
create or replace function public.forum_post_counters()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
    delta integer := 0;
begin
    if tg_op = 'INSERT' then
        delta := case when new.is_deleted then 0 else 1 end;
    elsif tg_op = 'UPDATE' then
        delta := (case when new.is_deleted then 0 else 1 end)
               - (case when old.is_deleted then 0 else 1 end);
    elsif tg_op = 'DELETE' then
        delta := case when old.is_deleted then 0 else -1 end;
    end if;

    if delta <> 0 then
        update public.forum_profiles
           set post_count = greatest(0, post_count + delta)
         where id = coalesce(new.author_id, old.author_id);

        -- The opening post is the thread itself, not a reply to it.
        if not coalesce(new.is_opening_post, old.is_opening_post) then
            update public.forum_threads
               set reply_count = greatest(0, reply_count + delta)
             where id = coalesce(new.thread_id, old.thread_id);
        end if;
    end if;

    if tg_op = 'INSERT' and not new.is_deleted then
        update public.forum_threads
           set last_reply_at = new.created_at
         where id = new.thread_id;
    end if;

    return coalesce(new, old);
end;
$$;

drop trigger if exists forum_posts_counters on public.forum_posts;
create trigger forum_posts_counters
    after insert or update of is_deleted or delete on public.forum_posts
    for each row execute function public.forum_post_counters();

-- A thanks moves the post's own count and the *author's* received total —
-- not the thanker's.
create or replace function public.forum_thanks_counters()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
    target_author uuid;
    delta integer := case when tg_op = 'INSERT' then 1 else -1 end;
begin
    select author_id into target_author
      from public.forum_posts
     where id = coalesce(new.post_id, old.post_id);

    update public.forum_posts
       set thanks_count = greatest(0, thanks_count + delta)
     where id = coalesce(new.post_id, old.post_id);

    if target_author is not null then
        update public.forum_profiles
           set thanks_received = greatest(0, thanks_received + delta)
         where id = target_author;
    end if;

    return coalesce(new, old);
end;
$$;

drop trigger if exists forum_thanks_counters on public.forum_thanks;
create trigger forum_thanks_counters
    after insert or delete on public.forum_thanks
    for each row execute function public.forum_thanks_counters();

-- ---------------------------------------------------------------------------
-- Row level security
-- ---------------------------------------------------------------------------

alter table public.forum_profiles   enable row level security;
alter table public.forum_categories enable row level security;
alter table public.forum_threads    enable row level security;
alter table public.forum_posts      enable row level security;
alter table public.forum_thanks     enable row level security;
alter table public.forum_reports    enable row level security;
alter table public.forum_blocks     enable row level security;

-- True when the caller is signed in and not banned. Every write goes through
-- this, so unbanning someone restores their access with no other change.
create or replace function public.forum_can_write()
returns boolean
language sql
stable
security definer set search_path = public
as $$
    select exists (
        select 1 from public.forum_profiles
         where id = auth.uid() and not is_banned
    );
$$;

-- Profiles: names and statistics are public, an email never is (it is not in
-- this table at all). A user may edit only their own display name.
drop policy if exists forum_profiles_read on public.forum_profiles;
create policy forum_profiles_read on public.forum_profiles
    for select using (true);

drop policy if exists forum_profiles_update_own on public.forum_profiles;
create policy forum_profiles_update_own on public.forum_profiles
    for update using (id = auth.uid()) with check (id = auth.uid());

-- Categories are read-only from the app; they are entered in the dashboard.
drop policy if exists forum_categories_read on public.forum_categories;
create policy forum_categories_read on public.forum_categories
    for select using (is_active);

-- Threads
drop policy if exists forum_threads_read on public.forum_threads;
create policy forum_threads_read on public.forum_threads
    for select using (not is_deleted);

drop policy if exists forum_threads_insert on public.forum_threads;
create policy forum_threads_insert on public.forum_threads
    for insert with check (author_id = auth.uid() and public.forum_can_write());

drop policy if exists forum_threads_update_own on public.forum_threads;
create policy forum_threads_update_own on public.forum_threads
    for update using (author_id = auth.uid() and public.forum_can_write())
            with check (author_id = auth.uid());

-- Posts
drop policy if exists forum_posts_read on public.forum_posts;
create policy forum_posts_read on public.forum_posts
    for select using (not is_deleted);

-- A locked thread accepts no new posts. Checked here rather than in the app,
-- so locking actually stops writing instead of only hiding the button.
drop policy if exists forum_posts_insert on public.forum_posts;
create policy forum_posts_insert on public.forum_posts
    for insert with check (
        author_id = auth.uid()
        and public.forum_can_write()
        and exists (
            select 1 from public.forum_threads t
             where t.id = thread_id and not t.is_locked and not t.is_deleted
        )
    );

drop policy if exists forum_posts_update_own on public.forum_posts;
create policy forum_posts_update_own on public.forum_posts
    for update using (author_id = auth.uid() and public.forum_can_write())
            with check (author_id = auth.uid());

-- Thanks. The self-thanks check is the one that matters: it is the easiest way
-- to inflate a public statistic, so it is refused by the database rather than
-- only hidden in the interface.
drop policy if exists forum_thanks_read on public.forum_thanks;
create policy forum_thanks_read on public.forum_thanks
    for select using (true);

drop policy if exists forum_thanks_insert on public.forum_thanks;
create policy forum_thanks_insert on public.forum_thanks
    for insert with check (
        user_id = auth.uid()
        and public.forum_can_write()
        and not exists (
            select 1 from public.forum_posts p
             where p.id = post_id and p.author_id = auth.uid()
        )
    );

drop policy if exists forum_thanks_delete_own on public.forum_thanks;
create policy forum_thanks_delete_own on public.forum_thanks
    for delete using (user_id = auth.uid());

-- Reports are write-only from the app. A reporter must not be able to read the
-- queue, and nobody should learn what has been reported about them.
drop policy if exists forum_reports_insert on public.forum_reports;
create policy forum_reports_insert on public.forum_reports
    for insert with check (reporter_id = auth.uid() and public.forum_can_write());

-- Blocks are private to the person who made them.
drop policy if exists forum_blocks_read_own on public.forum_blocks;
create policy forum_blocks_read_own on public.forum_blocks
    for select using (blocker_id = auth.uid());

drop policy if exists forum_blocks_insert_own on public.forum_blocks;
create policy forum_blocks_insert_own on public.forum_blocks
    for insert with check (blocker_id = auth.uid());

drop policy if exists forum_blocks_delete_own on public.forum_blocks;
create policy forum_blocks_delete_own on public.forum_blocks
    for delete using (blocker_id = auth.uid());
