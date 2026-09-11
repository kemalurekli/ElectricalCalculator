-- VoltageBoard — in-app error reports.
--
-- Run this once in the Supabase SQL editor, as the project owner. It is
-- idempotent, like every file here: re-running it after an edit is safe.
--
-- This is not the forum's report queue. `forum_reports` is about a *message*
-- somebody wrote and belongs to moderation. This is about the *app* — a
-- calculator whose figure looks wrong, a theory topic with a mistake in it, a
-- table whose units are off. Different reader, different lifetime, different
-- table.
--
-- Nothing here can be read by the app. The insert policy at the bottom is the
-- only way in, and there is deliberately no select policy: the anon key is
-- public by design, so anything readable is readable by everyone.

-- ---------------------------------------------------------------------------
-- The table
-- ---------------------------------------------------------------------------

create table if not exists public.app_feedback (
    id          uuid        primary key default gen_random_uuid(),
    created_at  timestamptz not null default now(),

    -- Null when nobody was signed in, which is most of the time: the app is
    -- offline apart from the forum, and reporting a mistake must not require
    -- an account. Detached rather than deleted when an account goes, for the
    -- same reason the forum keeps its reports — a queue that empties itself
    -- when somebody leaves is not a queue.
    user_id     uuid        references auth.users (id) on delete set null,

    -- A random id made once per installation, kept on the device. Not an
    -- identity: it survives no reinstall and is attached to no name. It exists
    -- so the rate limit below has something to count, and so twenty reports of
    -- the same thing can be told from one person pressing send twenty times.
    install_id  text        not null check (char_length(install_id) between 8 and 64),

    -- Which screen the reader was on, as a stable untranslated key:
    --   calculator:voltage_drop   theory:ohms_law   reference:cable_ampacity
    --   converter                 glossary
    -- The key rather than the title, because titles are translated and the
    -- same screen would arrive under twelve different names.
    area        text        not null check (char_length(area) between 2 and 64),

    message     text        not null check (char_length(trim(message)) between 5 and 2000),

    -- Diagnostics, not data about a person. Without them "the PDF is broken"
    -- cannot be acted on: it matters whether it is broken on iOS, on 1.4.2, in
    -- Polish. Nothing the reader typed into a calculator is here, on purpose.
    app_version text        not null default '',
    platform    text        not null check (platform in ('android', 'ios')),
    locale      text        not null default '',

    -- Set by hand when the report has been dealt with. The index below is
    -- partial on it, so the open queue stays cheap however long the table gets.
    resolved_at timestamptz
);

create index if not exists app_feedback_open_idx
    on public.app_feedback (created_at desc) where resolved_at is null;

create index if not exists app_feedback_area_idx
    on public.app_feedback (area, created_at desc);

-- ---------------------------------------------------------------------------
-- Rate limit
-- ---------------------------------------------------------------------------

-- Anyone holding the anon key can insert here, and the anon key is in the
-- shipped app. Row level security decides *what* may be written; it has no
-- opinion on *how much*, so this does.
--
-- `security definer` because the check has to count rows in a table that
-- nobody is allowed to read. It is the narrowest possible definer function:
-- one count, on one table, by one column.
create or replace function public.app_feedback_rate_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    recent integer;
begin
    select count(*) into recent
      from public.app_feedback
     where install_id = new.install_id
       and created_at > now() - interval '1 hour';

    if recent >= 5 then
        -- 54000 is program_limit_exceeded, which PostgREST returns as a 500.
        -- The app treats any failure the same way — it says the report could
        -- not be sent and keeps what was typed.
        raise exception 'app_feedback: five reports an hour is enough'
            using errcode = '54000';
    end if;

    return new;
end;
$$;

drop trigger if exists app_feedback_rate_limit on public.app_feedback;
create trigger app_feedback_rate_limit
    before insert on public.app_feedback
    for each row execute function public.app_feedback_rate_limit();

-- ---------------------------------------------------------------------------
-- Row level security
-- ---------------------------------------------------------------------------

alter table public.app_feedback enable row level security;

grant insert on public.app_feedback to anon, authenticated;

-- Write-only, and one direction only. A reporter cannot read the queue, edit a
-- report after sending it, or delete one — there are no policies for select,
-- update or delete, and with RLS on, no policy means no.
drop policy if exists app_feedback_insert on public.app_feedback;
create policy app_feedback_insert on public.app_feedback
    for insert to anon, authenticated
    with check (
        -- Signed in: the row is yours or it is nobody's. Nobody can file a
        -- report under someone else's name.
        (user_id is null or user_id = auth.uid())
    );

-- ---------------------------------------------------------------------------
-- Reading the queue
-- ---------------------------------------------------------------------------

-- In a schema PostgREST does not expose, so it cannot be reached with the anon
-- key however the grants drift. Read it from the SQL editor, which runs as the
-- owner:
--
--     select * from private.app_feedback_inbox where resolved_at is null;
--
create schema if not exists private;
revoke all on schema private from anon, authenticated;

create or replace view private.app_feedback_inbox as
select
    f.created_at,
    f.area,
    f.message,
    -- Who, in the two forms that exist: the name they chose for the forum, and
    -- the address the account was opened with. Both null for a report sent
    -- without signing in.
    p.display_name,
    u.email,
    f.platform,
    f.app_version,
    f.locale,
    f.install_id,
    f.resolved_at,
    f.id
  from public.app_feedback f
  left join auth.users            u on u.id = f.user_id
  left join public.forum_profiles p on p.id = f.user_id
 order by f.created_at desc;

revoke all on private.app_feedback_inbox from anon, authenticated;
