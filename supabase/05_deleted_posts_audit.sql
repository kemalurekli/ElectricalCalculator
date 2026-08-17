-- Keeps what was deleted, who deleted it, and when.
-- Run in the Supabase SQL editor after 01_forum_schema.sql.
--
-- forum_posts.is_deleted hides a message from the app, but the row stays and
-- an edit can still overwrite the body — so the record of what was actually
-- said disappears quietly. This table copies the message out at the moment it
-- goes, which is the only point where the original text is still there.
--
-- It exists for moderation: someone reports a message, the author withdraws it,
-- and without this there is nothing left to review. It is written by a trigger
-- rather than by the app, so it records the deletion even when the app is not
-- the thing doing the deleting.

create table if not exists public.forum_deleted_posts (
    id          uuid primary key default gen_random_uuid(),
    post_id     uuid        not null,
    thread_id   uuid        not null,
    thread_title text,
    author_id   uuid,
    author_name text,
    deleted_by  uuid,
    body        text        not null,
    posted_at   timestamptz,
    deleted_at  timestamptz not null default now(),
    -- 'withdrawn' — the author hid it from the thread (is_deleted set)
    -- 'purged'    — the row itself went, which today means account deletion
    reason      text        not null
);

-- The queue is read newest first, and almost always for one person or one
-- thread at a time.
create index if not exists forum_deleted_posts_deleted_at_idx
    on public.forum_deleted_posts (deleted_at desc);
create index if not exists forum_deleted_posts_author_idx
    on public.forum_deleted_posts (author_id);

-- Names are copied in rather than joined. The whole point is to survive the
-- author's account being deleted, and a foreign key to a profile that is about
-- to disappear would either block the deletion or take the record with it.
create or replace function public.forum_record_deleted_post()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    withdrawn boolean := tg_op = 'UPDATE';
    row_post  public.forum_posts := case when withdrawn then new else old end;
begin
    -- On UPDATE, only the transition into deleted counts. Editing an already
    -- hidden message must not file it a second time.
    if withdrawn and (old.is_deleted or not new.is_deleted) then
        return new;
    end if;

    insert into public.forum_deleted_posts (
        post_id, thread_id, thread_title, author_id, author_name,
        deleted_by, body, posted_at, reason
    )
    select row_post.id,
           row_post.thread_id,
           t.title,
           row_post.author_id,
           p.display_name,
           auth.uid(),
           row_post.body,
           row_post.created_at,
           case when withdrawn then 'withdrawn' else 'purged' end
    from (select 1) as _
    left join public.forum_threads  t on t.id = row_post.thread_id
    left join public.forum_profiles p on p.id = row_post.author_id;

    return case when withdrawn then new else old end;
end;
$$;

drop trigger if exists forum_posts_record_withdrawal on public.forum_posts;
create trigger forum_posts_record_withdrawal
    after update of is_deleted on public.forum_posts
    for each row execute function public.forum_record_deleted_post();

drop trigger if exists forum_posts_record_purge on public.forum_posts;
create trigger forum_posts_record_purge
    before delete on public.forum_posts
    for each row execute function public.forum_record_deleted_post();

-- Nobody reads this from the app. It holds the text of messages people chose to
-- remove, so exposing it to any client would undo the removal — the anon and
-- authenticated roles get nothing, and RLS with no policy denies everything.
alter table public.forum_deleted_posts enable row level security;
revoke all on public.forum_deleted_posts from anon, authenticated;
