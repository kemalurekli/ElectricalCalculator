-- Proves the forum's rules actually hold. Run in the Supabase SQL editor after
-- 01_forum_schema.sql.
--
-- No client test can check any of this. The app only ever sees what the
-- policies let through, so a policy that is too permissive looks exactly like
-- one that is correct — from the app's side, everything simply works.
--
-- The script creates two throwaway users, asserts, and rolls everything back.
--
-- IT ALWAYS ENDS IN A RED ERROR, AND THAT IS CORRECT. Rolling back is the only
-- way to leave no fixtures behind, and a rollback means raising. Read the
-- message, not the colour:
--
--   PASSED — ...   every check held
--   anything else  the rule named in the message does not hold

do $$
declare
    alice uuid := gen_random_uuid();
    bob   uuid := gen_random_uuid();
    cat   uuid;
    thr   uuid;
    post  uuid;
    ok    boolean;
    n     integer;
begin
    -- Fixtures are inserted as the owner, bypassing RLS on purpose: the point
    -- is to test reads and writes made *as a user*, not the seeding.
    insert into auth.users (id, instance_id, aud, role, email,
                            encrypted_password, email_confirmed_at,
                            created_at, updated_at,
                            raw_app_meta_data, raw_user_meta_data)
    values (alice, '00000000-0000-0000-0000-000000000000', 'authenticated',
            'authenticated', 'alice@example.test', '', now(), now(), now(),
            '{}'::jsonb, '{"full_name":"Alice"}'::jsonb),
           (bob, '00000000-0000-0000-0000-000000000000', 'authenticated',
            'authenticated', 'bob@example.test', '', now(), now(), now(),
            '{}'::jsonb, '{"full_name":"Bob"}'::jsonb);

    -- The sign-up trigger should have made both profiles.
    select count(*) into n from public.forum_profiles where id in (alice, bob);
    if n <> 2 then
        raise exception 'sign-up trigger did not create a profile for each user (got %)', n;
    end if;

    insert into public.forum_categories (key, language, title)
    values ('general', 'tr', 'Genel') returning id into cat;

    insert into public.forum_threads (category_id, author_id, language, title)
    values (cat, alice, 'tr', 'Test konusu baslik') returning id into thr;

    insert into public.forum_posts (thread_id, author_id, body, is_opening_post)
    values (thr, alice, 'Acilis mesaji', true) returning id into post;

    -- ---- counters ------------------------------------------------------
    select post_count into n from public.forum_profiles where id = alice;
    if n <> 1 then raise exception 'post_count should be 1 after one post, got %', n; end if;

    select reply_count into n from public.forum_threads where id = thr;
    if n <> 0 then raise exception 'the opening post must not count as a reply, got %', n; end if;

    insert into public.forum_posts (thread_id, author_id, body)
    values (thr, bob, 'Cevap');

    select reply_count into n from public.forum_threads where id = thr;
    if n <> 1 then raise exception 'reply_count should be 1, got %', n; end if;

    -- ---- thanks move the author's total, not the thanker's --------------
    insert into public.forum_thanks (post_id, user_id) values (post, bob);

    select thanks_count into n from public.forum_posts where id = post;
    if n <> 1 then raise exception 'thanks_count should be 1, got %', n; end if;

    select thanks_received into n from public.forum_profiles where id = alice;
    if n <> 1 then raise exception 'the author should have received the thanks, got %', n; end if;

    select thanks_received into n from public.forum_profiles where id = bob;
    if n <> 0 then raise exception 'the thanker must not gain anything, got %', n; end if;

    delete from public.forum_thanks where post_id = post and user_id = bob;
    select thanks_received into n from public.forum_profiles where id = alice;
    if n <> 0 then raise exception 'removing a thanks should take it back, got %', n; end if;

    -- ---- now as a signed-in user, with policies enforced ----------------
    set local role authenticated;

    -- Bob may not edit Alice's thread.
    perform set_config('request.jwt.claims', json_build_object('sub', bob, 'role', 'authenticated')::text, true);
    update public.forum_threads set title = 'Ele gecirildi' where id = thr;
    if found then raise exception 'a user was able to edit another user''s thread'; end if;

    -- Nobody may thank their own post.
    perform set_config('request.jwt.claims', json_build_object('sub', alice, 'role', 'authenticated')::text, true);
    begin
        insert into public.forum_thanks (post_id, user_id) values (post, alice);
        raise exception 'a user was able to thank their own post';
    exception when insufficient_privilege or check_violation then
        null;
    end;

    -- A banned profile may not write.
    reset role;
    update public.forum_profiles set is_banned = true where id = bob;
    set local role authenticated;
    perform set_config('request.jwt.claims', json_build_object('sub', bob, 'role', 'authenticated')::text, true);
    begin
        insert into public.forum_posts (thread_id, author_id, body)
        values (thr, bob, 'Banliyken yazilan mesaj');
        raise exception 'a banned user was able to post';
    exception when insufficient_privilege then
        null;
    end;

    -- A locked thread accepts nothing further.
    reset role;
    update public.forum_profiles set is_banned = false where id = bob;
    update public.forum_threads set is_locked = true where id = thr;
    set local role authenticated;
    perform set_config('request.jwt.claims', json_build_object('sub', bob, 'role', 'authenticated')::text, true);
    begin
        insert into public.forum_posts (thread_id, author_id, body)
        values (thr, bob, 'Kilitli konuya mesaj');
        raise exception 'a locked thread accepted a post';
    exception when insufficient_privilege then
        null;
    end;

    -- The report queue is write-only from the app side.
    select exists (select 1 from public.forum_reports) into ok;
    if ok then raise exception 'a user was able to read the report queue'; end if;

    -- ---- deletion --------------------------------------------------------
    -- None of this could be checked before 07: there were no delete policies
    -- at all, so every one of these would have failed for the same reason.
    reset role;
    update public.forum_threads set is_locked = false where id = thr;
    set local role authenticated;

    -- Bob may not delete Alice's message.
    perform set_config('request.jwt.claims', json_build_object('sub', bob, 'role', 'authenticated')::text, true);
    delete from public.forum_posts where id = post;
    if found then raise exception 'a user was able to delete another user''s post'; end if;

    -- Alice may delete her own, and it lands in the archive.
    perform set_config('request.jwt.claims', json_build_object('sub', alice, 'role', 'authenticated')::text, true);
    delete from public.forum_posts where id = post;
    if not found then raise exception 'an author could not delete their own post'; end if;

    reset role;
    select count(*) into n from public.forum_deleted_posts where post_id = post;
    if n <> 1 then raise exception 'a deleted post was not archived (got % rows)', n; end if;
    set local role authenticated;

    -- The thread still has Bob's reply, so it must refuse to go.
    perform set_config('request.jwt.claims', json_build_object('sub', alice, 'role', 'authenticated')::text, true);
    delete from public.forum_threads where id = thr;
    if found then raise exception 'a thread with replies was deleted'; end if;

    -- With the replies gone it is hers alone again, and it may go.
    reset role;
    delete from public.forum_posts where thread_id = thr;
    set local role authenticated;
    perform set_config('request.jwt.claims', json_build_object('sub', alice, 'role', 'authenticated')::text, true);
    delete from public.forum_threads where id = thr;
    if not found then raise exception 'an author could not delete their own reply-free thread'; end if;

    reset role;
    select count(*) into n from public.forum_deleted_threads where thread_id = thr;
    if n <> 1 then raise exception 'a deleted thread was not archived (got % rows)', n; end if;
    set local role authenticated;

    reset role;

    -- The verdict is carried by the exception itself rather than by a NOTICE.
    -- Rolling back is the only way to leave no fixtures behind, and a rollback
    -- means raising — so the SQL editor is going to show something red either
    -- way. It may as well say what happened: a message starting with PASSED is
    -- success, and anything else is the rule that did not hold.
    raise exception
        'PASSED — every forum policy and counter check held. Fixtures rolled back; this red message is the expected result.';
end $$;
