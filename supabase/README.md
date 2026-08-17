# Supabase — forum backend

Run these in the Supabase SQL editor, in order, as the project owner.

| File | What it does | When |
|---|---|---|
| `01_forum_schema.sql` | Tables, indexes, counter triggers, row level security | Once, and again after any edit — it is idempotent |
| `02_verify_policies.sql` | Proves the policies and counters hold | After every change to `01` |

`02` creates two throwaway users, asserts, and rolls everything back. It raises
on the first rule that does not hold; reaching `All forum policy and counter
checks passed.` means all of them do. The final `rollback:` exception is
deliberate — it is how the script guarantees it leaves nothing behind.

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
