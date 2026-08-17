-- Lets a signed-in user delete their own account, and nobody else's.
-- Run in the Supabase SQL editor after 01_forum_schema.sql.
--
-- Play requires an in-app route to account deletion for any app that holds an
-- account. It has to be real deletion, not a support address.
--
-- The app holds only the anon key, which cannot reach auth.users — and giving
-- it that reach would be a far worse trade than the inconvenience. So deletion
-- runs here instead, as `security definer`, with the caller taken from the JWT
-- rather than from an argument. There is deliberately no parameter: a function
-- that accepts a user id is a function that can be asked to delete someone
-- else's, whatever the policy around it says.

create or replace function public.forum_delete_account()
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    caller uuid := auth.uid();
begin
    if caller is null then
        raise exception 'forum_delete_account requires a signed-in caller';
    end if;

    -- The content goes first. Posts are hard-deleted here rather than flagged:
    -- the soft delete elsewhere exists so reported content survives review,
    -- and that reasoning does not extend to someone withdrawing entirely.
    delete from public.forum_thanks where user_id = caller;
    delete from public.forum_posts where author_id = caller;
    delete from public.forum_threads where author_id = caller;
    delete from public.forum_blocks where blocker_id = caller or blocked_id = caller;

    -- Reports are kept, with the reporter detached. A moderation queue that
    -- empties itself whenever a reporter leaves is not a moderation queue.
    update public.forum_reports set reporter_id = null where reporter_id = caller;

    delete from public.forum_profiles where id = caller;
    delete from auth.users where id = caller;
end;
$$;

-- Callable by signed-in users only. `security definer` means the body runs with
-- the owner's rights, so the grant is the entire access check.
revoke all on function public.forum_delete_account() from public, anon;
grant execute on function public.forum_delete_account() to authenticated;

-- forum_reports.reporter_id must tolerate the detachment above.
alter table public.forum_reports alter column reporter_id drop not null;
