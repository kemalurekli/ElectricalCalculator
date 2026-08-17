# Supabase — forum backend

Run these in the Supabase SQL editor, in order, as the project owner.

| File | What it does | When |
|---|---|---|
| `01_forum_schema.sql` | Tables, indexes, counter triggers, row level security | First |
| `02_verify_policies.sql` | Proves the policies and counters hold | After any schema change |
| `03_seed_content.sql` | Categories and starter threads in both languages | Once, re-runnable |
| `04_account_deletion.sql` | `forum_delete_account()` — Play requires an in-app route | After `01` |
| `05_deleted_posts_audit.sql` | Archives removed messages | After `01` |
| `06_fix_soft_delete_policy.sql` | Lets an author's delete return its own row | After `01` |
| `07_deletion.sql` | Real deletion: DELETE policies, thread archive | Last |

## Run them in order, and re-run the whole sequence

Every file is individually idempotent, but they are **not independent**. `01`
defines `forum_posts_read` and `forum_threads_read` narrowly, and `06`/`07`
widen them so that a delete can return its own row. Running `01` on its own
after the others silently puts the narrow versions back, and deletion starts
failing with

    new row violates row-level security policy for table "forum_posts"

which reads like a permissions bug and is not one. `07` restates those two
policies at the end of the sequence for exactly this reason: whatever else was
run, finishing with `07` leaves them correct.

`02` creates two throwaway users, asserts, and rolls everything back.

**It always finishes with a red error, and that is the correct outcome.**
Rolling back is the only way to leave no fixtures behind, and a rollback means
raising — so read the message rather than the colour:

- `PASSED — every forum policy and counter check held…` — everything holds
- anything else — the message names the rule that does not

## Why the verification script exists

No test in the app can check any of this. The app only ever sees what the
policies let through, so a policy that is too permissive looks exactly like one
that is correct — from the client's side, everything simply works. The rules
worth proving are the ones an attacker would try:

- editing someone else's thread
- thanking your own post to inflate a public statistic
- posting while banned, or into a locked thread
- reading the report queue

## Keys

Two keys, and only one of them belongs anywhere near the app.

- **anon** — public by design. It goes in `local.properties` and reaches the
  app through `BuildConfig`. It grants nothing on its own; the policies above
  are what protect the data.
- **service_role** — bypasses row level security entirely. It belongs in the
  dashboard and in server-side tooling. It must never be committed, never be
  put in `local.properties`, and never be pasted into a chat.

```properties
# local.properties — already gitignored
supabase.url=https://<project>.supabase.co
supabase.anonKey=<anon key>
```

## Categories and seed content

Categories are entered by hand in the dashboard. A category belongs to one
language, so the same subject is two rows:

```sql
insert into public.forum_categories (key, language, title, description, position) values
  ('installations', 'tr', 'Tesisat',      'Saha uygulamaları ve montaj',        10),
  ('installations', 'en', 'Installations', 'Field practice and installation',   10);
```

A forum with three threads reads as abandoned. Seed 20–30 threads per language
before the feature ships.
