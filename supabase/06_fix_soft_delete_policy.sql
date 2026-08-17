-- Lets an author actually withdraw their own message.
-- Run in the Supabase SQL editor after 01_forum_schema.sql.
--
-- The bug: editing a post worked, but deleting one failed with
--
--     new row violates row-level security policy for table "forum_posts"
--
-- Same row, same author, same UPDATE policy — the only difference is which
-- column changed. That is the whole clue.
--
-- forum_posts_read is `using (not is_deleted)`. Setting is_deleted makes the
-- row invisible to its own author, and PostgREST's UPDATE returns the rows it
-- touched, so the statement ends by trying to hand back a row that the select
-- policy now forbids. The write was legitimate; the row disappearing out from
-- under the same statement is what failed.
--
-- The fix is to let authors see their own withdrawn posts. Nothing changes in
-- the app — the queries already filter `is_deleted = false` — and nobody else
-- gains any visibility at all.

drop policy if exists forum_posts_read on public.forum_posts;
create policy forum_posts_read on public.forum_posts
    for select using (
        not is_deleted
        -- Only your own, and only the ones you removed yourself. This is what
        -- makes the withdrawal a legal statement rather than one that erases
        -- its own result.
        or author_id = auth.uid()
    );

-- Threads have the identical shape, and would fail the identical way the first
-- time anyone deletes one. Fixed here rather than waiting for the report.
drop policy if exists forum_threads_read on public.forum_threads;
create policy forum_threads_read on public.forum_threads
    for select using (
        not is_deleted
        or author_id = auth.uid()
    );
