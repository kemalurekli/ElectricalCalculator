-- ElecToolkit forum — a second round of seed content.
--
-- Run after 03_seed_content.sql. Re-runnable on its own: it removes only what
-- it created, so running it again replaces its threads rather than doubling
-- them, and it never touches 03's.
--
-- Three more voices, on fixed UUIDs beginning 00000000-0000-4000-8001 (03 uses
-- ...8000). That one digit is what keeps the two scripts' cleanups apart.
--
-- One category is deliberately overfilled. "Arıza Bulma" ends up with more than
-- twenty-five threads, which is the page size — so it is the only place where
-- the reader can scroll to the end of a page and see whether the next one
-- arrives. Without a category that large, paging is a feature nobody can look at.
--
-- The same two decisions 03 raises still stand: these accounts accumulate post
-- counts and thanks that no person earned, and the names read as real people.
-- Rename them to something openly editorial before shipping if that sits badly.

begin;

-- ---------------------------------------------------------------------
-- Cleanup of any previous run of THIS script
-- ---------------------------------------------------------------------
delete from auth.users where id in (
    '00000000-0000-4000-8001-000000000001',
    '00000000-0000-4000-8001-000000000002',
    '00000000-0000-4000-8001-000000000003'
);

-- ---------------------------------------------------------------------
-- Three more accounts
-- ---------------------------------------------------------------------
insert into auth.users (id, instance_id, aud, role, email, encrypted_password,
                        email_confirmed_at, created_at, updated_at,
                        raw_app_meta_data, raw_user_meta_data)
values
  ('00000000-0000-4000-8001-000000000001', '00000000-0000-0000-0000-000000000000',
   'authenticated', 'authenticated', 'seed.hakan@example.invalid', '',
   now() - interval '120 days', now() - interval '120 days', now(),
   '{}'::jsonb, '{"full_name":"Hakan Demirtaş"}'::jsonb),
  ('00000000-0000-4000-8001-000000000002', '00000000-0000-0000-0000-000000000000',
   'authenticated', 'authenticated', 'seed.sibel@example.invalid', '',
   now() - interval '95 days', now() - interval '95 days', now(),
   '{}'::jsonb, '{"full_name":"Sibel Korkmaz"}'::jsonb),
  ('00000000-0000-4000-8001-000000000003', '00000000-0000-0000-0000-000000000000',
   'authenticated', 'authenticated', 'seed.grace@example.invalid', '',
   now() - interval '80 days', now() - interval '80 days', now(),
   '{}'::jsonb, '{"full_name":"Grace Okonkwo"}'::jsonb);

-- ---------------------------------------------------------------------
-- Threads with real conversation
--
-- Every language stays where it belongs: Turkish bodies under Turkish
-- categories, English under English. A mixed thread breaks the one rule the
-- whole forum is built on, and it happened once already in 03.
-- ---------------------------------------------------------------------
do $$
declare
    cat uuid;
    thr uuid;
    op  uuid;

    hakan uuid := '00000000-0000-4000-8001-000000000001';
    sibel uuid := '00000000-0000-4000-8001-000000000002';
    grace uuid := '00000000-0000-4000-8001-000000000003';

    -- 03's people, so the two rounds read as one forum rather than two.
    mehmet uuid := '00000000-0000-4000-8000-000000000001';
    elif   uuid := '00000000-0000-4000-8000-000000000002';
    ryan   uuid := '00000000-0000-4000-8000-000000000003';

    filler text[] := array[
        'Panoda kablo etiketlemesi için pratik bir düzen öneriniz var mı?',
        'Nemli ortamda buat seçerken IP sınıfı dışında neye bakmalı?',
        'Eski tesisatta sıfır ile toprak birleşmiş, nereden başlamalı?',
        'Priz devresinde hep aynı priz ısınıyor, sebebi ne olabilir?',
        'Aydınlatma hattında sigorta atmadan lamba yanıyor, neden?',
        'Trifaze motorda iki faz gelip biri gelmezse ne gözlenir?',
        'Kaçak akım rölesi yağmurdan sonra atıyor, nereye bakmalı?',
        'Uzatma kablosunda ısınma hangi noktada tehlikeli sayılır?',
        'Klemens mi, yaylı konnektör mü, hangisi daha kalıcı?',
        'Topraklama direncini sahada ölçmenin en güvenilir yolu?',
        'Pano içi sıcaklık kaç dereceden sonra sorun çıkarır?',
        'Kablo kanalında doluluk oranını nasıl kontrol ediyorsunuz?',
        'Şalter markası karıştırmak seçiciliği bozar mı?',
        'Ölçü aletinin kendi hatası ölçümü ne kadar etkiler?',
        'Buat içinde kaç bağlantı fazla sayılır?',
        'Zayıf akım ile kuvvetli akımı aynı kanaldan geçirmek?',
        'Sıva altı tesisatta kablo derinliği ne olmalı?',
        'Eski binada kesit yetersizse önce neyi değiştirmeli?',
        'Jeneratör devreye girince neden bazı cihazlar kapanıyor?',
        'Sayaç öncesi ve sonrası koruma nasıl ayrılmalı?',
        'UPS çıkışında topraklama nasıl kurulmalı?',
        'Faz sırası yanlışsa hangi cihazlar zarar görür?',
        'Kablo eki yapmak zorunda kalınca en doğru yöntem?',
        'Rutubetli bodrumda priz devresi nasıl korunmalı?'
    ];
    bodies text[] := array[
        'Sahada sık karşılaştığım bir durum, tecrübesi olanların görüşünü merak ediyorum.',
        'Bu konuda net bir kaynak bulamadım, uygulamada nasıl çözüyorsunuz?',
        'Ölçüm yaptım ama sonucu yorumlamakta zorlandım, yardımcı olur musunuz?',
        'Müşteriye ne söyleyeceğime karar veremedim, sizce doğru yaklaşım ne?'
    ];
    answers text[] := array[
        'Önce ölçmeden karar vermeyin. Aynı belirtiyi veren en az üç ayrı sebep var ve hangisi olduğunu yalnızca ölçüm söyler.',
        'Bende benzer bir iş olmuştu, sorun tesisatın kendisinde değil bağlantı noktasındaydı. Klemensleri sıkmak çözmüştü.',
        'Bunu standarda göre değerlendirmek gerekir. Uygulamadaki referans bölümünde ilgili metin var, oradan bakabilirsiniz.',
        'Dikkat edilmesi gereken nokta yükün sürekli mi anlık mı olduğu. İkisi için önerilen çözüm birbirinden farklı.'
    ];
    i integer;
begin
    -- ---------- Arıza Bulma (tr): deliberately past one page ----------
    select id into cat from public.forum_categories
     where key = 'troubleshooting' and language = 'tr';

    for i in 1 .. array_length(filler, 1) loop
        insert into public.forum_threads (category_id, author_id, language, title,
                                          created_at, last_reply_at)
        values (cat,
                case (i % 3) when 0 then hakan when 1 then sibel else mehmet end,
                'tr',
                filler[i],
                now() - (i || ' days')::interval,
                now() - (i || ' days')::interval)
        returning id into thr;

        insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
        values (thr,
                case (i % 3) when 0 then hakan when 1 then sibel else mehmet end,
                bodies[1 + (i % array_length(bodies, 1))],
                true,
                now() - (i || ' days')::interval)
        returning id into op;

        -- Not every thread gets an answer. A forum where every question is
        -- answered within hours is not one anybody recognises.
        if i % 3 <> 0 then
            insert into public.forum_posts (thread_id, author_id, body, created_at)
            values (thr,
                    case (i % 2) when 0 then elif else hakan end,
                    answers[1 + (i % array_length(answers, 1))],
                    now() - (i || ' days')::interval + interval '6 hours');
        end if;

        -- A few thanks, never from the author of the post being thanked.
        if i % 4 = 0 then
            insert into public.forum_thanks (post_id, user_id) values (op, grace)
            on conflict do nothing;
        end if;
    end loop;

    -- ---------- Tesisat ve Montaj (tr) ----------
    select id into cat from public.forum_categories
     where key = 'installations' and language = 'tr';

    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, sibel, 'tr', 'Pano içinde kablo düzeni: estetik mi, erişim mi?',
            now() - interval '11 days', now() - interval '9 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, sibel, 'Panoyu çok toplu yapınca sonradan tek bir kabloyu çekmek işkenceye dönüyor. Siz düzeni ne kadar sıkı tutuyorsunuz? Kanal doluluğunu bilerek düşük mü bırakıyorsunuz?', true, now() - interval '11 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at) values
      (thr, hakan, 'Kanalı asla ağzına kadar doldurmuyorum. Bir yıl sonra o panoya dönen kişi ben olabilirim ve tek bir kabloyu çekebilmek, fotoğrafta güzel görünmekten daha değerli.', now() - interval '10 days'),
      (thr, mehmet, 'Bir de yedek pay bırakın. Kanal doluysa ilave devre için yeni kanal açmak gerekiyor ve o iş her zaman ilk montajdan pahalı çıkıyor.', now() - interval '9 days');
    insert into public.forum_thanks (post_id, user_id) values (op, hakan) on conflict do nothing;

    -- ---------- Proje ve Hesap (tr) ----------
    select id into cat from public.forum_categories
     where key = 'design' and language = 'tr';

    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, hakan, 'tr', 'Yük hesabında eş zamanlılık katsayısını kaç alıyorsunuz?',
            now() - interval '7 days', now() - interval '5 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, hakan, 'Konutta tüm devreleri toplayınca çok büyük bir sayı çıkıyor, ama gerçekte o yükün hepsi aynı anda çekilmiyor. Sahada hangi katsayıyla çalışıyorsunuz ve neye göre karar veriyorsunuz?', true, now() - interval '7 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at) values
      (thr, elif, 'Daire sayısına göre değişiyor. Tek daire için düşürmeye pek yer yok, ama on daireli bir blokta toplam yükün yarısına yakın bir değer gerçeğe daha yakın çıkıyor.', now() - interval '6 days'),
      (thr, sibel, 'Elektrikli araç şarjı bu hesabı bozuyor. O yük eş zamanlı ve uzun süreli, katsayıya dahil edilecek cinsten değil — ayrı düşünmek gerekiyor.', now() - interval '5 days');
    insert into public.forum_thanks (post_id, user_id) values (op, sibel) on conflict do nothing;

    -- ---------- Troubleshooting (en) ----------
    select id into cat from public.forum_categories
     where key = 'troubleshooting' and language = 'en';

    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, grace, 'en', 'RCD holds all week, trips every Monday morning',
            now() - interval '13 days', now() - interval '11 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, grace, 'A small workshop. The RCD is fine all week and trips within minutes of the first machine starting on Monday. Nothing has been added. Is this worth chasing as a fault, or is it just accumulated damp?', true, now() - interval '13 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at) values
      (thr, ryan, 'Damp over a cold weekend is the usual answer, and it is worth chasing anyway. Standing leakage that only shows when it rises is leakage the rest of the week too — you are just seeing it at its worst.', now() - interval '12 days'),
      (thr, grace, 'Measured it cold on Monday before anything ran: already most of the way to the trip threshold. Split the circuits and it is one heater. Thank you.', now() - interval '11 days');
    insert into public.forum_thanks (post_id, user_id) values (op, ryan) on conflict do nothing;

    -- ---------- Design & Calculations (en) ----------
    select id into cat from public.forum_categories
     where key = 'design' and language = 'en';

    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, ryan, 'en', 'Voltage drop on a long run: where do you stop derating?',
            now() - interval '6 days', now() - interval '4 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, ryan, 'A 90 m run to an outbuilding. Sizing on voltage drop pushes the cable up two sizes over what the load needs. At what point does everyone accept the drop and move the board instead?', true, now() - interval '6 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at) values
      (thr, grace, 'When the cable costs more than the sub-board, which on 90 m it usually does. A board at the far end also gives you local isolation, which you will want the first time something faults out there.', now() - interval '5 days'),
      (thr, ryan, 'That is the answer I did not want and the one I expected. Pricing both today.', now() - interval '4 days');

    -- ---------- Learning & Trade (en) ----------
    select id into cat from public.forum_categories
     where key = 'learning' and language = 'en';

    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, grace, 'en', 'Which test instrument first, on a limited budget?',
            now() - interval '3 days', now() - interval '2 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, grace, 'Starting out and cannot buy everything at once. Multimeter, clamp meter, insulation tester, loop tester — in what order did you actually end up needing them?', true, now() - interval '3 days');
    insert into public.forum_posts (thread_id, author_id, body, created_at) values
      (thr, ryan, 'Clamp meter earlier than most people say. A multimeter tells you a circuit is live; a clamp tells you what it is actually drawing, and that answers more questions on site than anything else at that price.', now() - interval '2 days');
end $$;

commit;

-- What this added, for checking afterwards:
--
--   select c.title, c.language, count(*) as threads
--     from public.forum_threads t
--     join public.forum_categories c on c.id = t.category_id
--    group by c.title, c.language
--    order by threads desc;
--
-- "Arıza Bulma" should be past twenty-five, which is the page size — scroll it
-- in the app to watch the second page arrive.
