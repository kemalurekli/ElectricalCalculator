-- Creates a thread and its opening message together, or not at all.
-- Run in the Supabase SQL editor after 07_deletion.sql.
--
-- The app did this as two separate inserts: forum_threads, then forum_posts
-- with is_opening_post. A connection that dropped between them left a thread
-- with no message in it — visible in the category, with a title and a reply
-- count, opening onto nothing. Its own KDoc named the failure and the code did
-- not guard it.
--
-- PostgREST has no transactions across requests, so the fix is to make it one
-- request. A function body is a single transaction: either both rows exist
-- afterwards or neither does.
--
-- security definer with the author taken from auth.uid() rather than from an
-- argument, for the same reason as forum_delete_account: a function that
-- accepts an author id is one that can be asked to post as somebody else.
-- Everything the RLS policies would have checked is checked here instead.

create or replace function public.forum_create_thread(
    p_category_id uuid,
    p_language    text,
    p_title       text,
    p_body        text
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    author uuid := auth.uid();
    new_thread uuid;
begin
    if author is null then
        raise exception 'forum_create_thread requires a signed-in caller';
    end if;

    if not public.forum_can_write() then
        raise exception 'this account may not post';
    end if;

    -- The category has to exist, be active, and be in the language claimed for
    -- the thread. Without this a caller could file a Turkish thread under an
    -- English category and break the one rule the whole forum is built on.
    if not exists (
        select 1 from public.forum_categories
         where id = p_category_id and is_active and language = p_language
    ) then
        raise exception 'no active category % in language %', p_category_id, p_language;
    end if;

    insert into public.forum_threads (category_id, author_id, language, title)
    values (p_category_id, author, p_language, p_title)
    returning id into new_thread;

    insert into public.forum_posts (thread_id, author_id, body, is_opening_post)
    values (new_thread, author, p_body, true);

    return new_thread;
end;
$$;

revoke all on function public.forum_create_thread(uuid, text, text, text) from public, anon;
grant execute on function public.forum_create_thread(uuid, text, text, text) to authenticated;
