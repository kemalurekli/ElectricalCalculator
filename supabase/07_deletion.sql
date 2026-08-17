-- Lets people actually remove what they wrote.
-- Run in the Supabase SQL editor after 06_fix_soft_delete_policy.sql.
--
-- Until now nothing could be deleted for real. forum_posts and forum_threads
-- had no DELETE policy at all, so the only removal was `is_deleted = true` —
-- the row stayed, and a thread whose opening post was withdrawn went on sitting
-- in its category with the title its author wanted gone. The only way to delete
-- a thread was to delete your entire account.
--
-- 05_deleted_posts_audit.sql changed what soft deletion was for. The archive
-- keeps the text, the author, who removed it and when, so keeping a hidden copy
-- in forum_posts as well preserves nothing that is not already preserved. The
-- BEFORE DELETE trigger it installed already files a hard delete as 'purged',
-- which means switching to real deletion needs no new archiving code.
--
-- is_deleted stays. Its remaining job is the one it is actually good at: a
-- moderator hiding reported content while it is looked at, without destroying
-- the thing under review.

-- ---------------------------------------------------------------- posts ----
drop policy if exists forum_posts_delete_own on public.forum_posts;
create policy forum_posts_delete_own on public.forum_posts
    for delete using (author_id = auth.uid() and public.forum_can_write());

-- -------------------------------------------------------------- threads ----
-- A thread can only be withdrawn while it is still only its author's. The
-- moment somebody answers, the thread stops being one person's to erase — the
-- replies are other people's work, and taking the question away leaves them
-- answering nothing.
--
-- The rule lives here rather than in the app. Hiding the button is not
-- enforcing anything: two people can be looking at the same empty thread, and
-- the one who taps delete a moment after the other taps send must lose.
drop policy if exists forum_threads_delete_own on public.forum_threads;
create policy forum_threads_delete_own on public.forum_threads
    for delete using (
        author_id = auth.uid()
        and public.forum_can_write()
        and reply_count = 0
    );

-- ------------------------------------------------- the threads' archive ----
-- Same shape and same reasoning as forum_deleted_posts: the names are copied
-- in rather than joined, so the record survives the author's account going.
create table if not exists public.forum_deleted_threads (
    id           uuid primary key default gen_random_uuid(),
    thread_id    uuid        not null,
    category_id  uuid,
    title        text        not null,
    author_id    uuid,
    author_name  text,
    deleted_by   uuid,
    language     text,
    posted_at    timestamptz,
    reply_count  integer     not null default 0,
    deleted_at   timestamptz not null default now(),
    -- 'withdrawn' — the author deleted their own, reply-free thread
    -- 'purged'    — the row went with the account
    reason       text        not null
);

create index if not exists forum_deleted_threads_deleted_at_idx
    on public.forum_deleted_threads (deleted_at desc);

create or replace function public.forum_record_deleted_thread()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.forum_deleted_threads (
        thread_id, category_id, title, author_id, author_name,
        deleted_by, language, posted_at, reply_count, reason
    )
    select old.id,
           old.category_id,
           old.title,
           old.author_id,
           p.display_name,
           auth.uid(),
           old.language,
           old.created_at,
           old.reply_count,
           -- Deleting your account removes threads that still have replies,
           -- which the ordinary delete policy would never allow. That is the
           -- only way the two can be told apart afterwards.
           case when old.reply_count = 0 then 'withdrawn' else 'purged' end
    from (select 1) as _
    left join public.forum_profiles p on p.id = old.author_id;

    return old;
end;
$$;

drop trigger if exists forum_threads_record_deletion on public.forum_threads;
create trigger forum_threads_record_deletion
    before delete on public.forum_threads
    for each row execute function public.forum_record_deleted_thread();

-- Nobody reads this from a client, for the same reason as the posts archive:
-- it holds what people chose to remove. RLS with no policy denies everything.
alter table public.forum_deleted_threads enable row level security;
revoke all on public.forum_deleted_threads from anon, authenticated;

-- ------------------------------------------------- re-assert 06's policies --
-- 01_forum_schema.sql defines these with the narrow `not is_deleted`, and it is
-- meant to be re-runnable. Re-running it silently reverts 06 and deletion
-- starts failing again with "new row violates row-level security policy" —
-- which took a while to diagnose the first time. Restating them here means the
-- last file in the sequence always leaves them correct.
drop policy if exists forum_posts_read on public.forum_posts;
create policy forum_posts_read on public.forum_posts
    for select using (not is_deleted or author_id = auth.uid());

drop policy if exists forum_threads_read on public.forum_threads;
create policy forum_threads_read on public.forum_threads
    for select using (not is_deleted or author_id = auth.uid());
