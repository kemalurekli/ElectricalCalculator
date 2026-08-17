-- ElecToolkit forum — categories and seed content.
--
-- Run after 01_forum_schema.sql. Re-runnable: it deletes its own previous
-- output first, so editing a thread and running again replaces it rather
-- than duplicating it.
--
-- The three seed accounts use fixed UUIDs beginning 00000000-0000-4000-8000,
-- which is what makes both the cleanup and this comment possible: everything
-- this script created can be removed in one statement.
--
-- A forum with three threads reads as abandoned, so this exists to give the
-- first real visitor something to read. Two things to decide before launch:
-- these accounts accumulate post counts and thanks that no person earned,
-- and the names read as real people. If that sits badly, rename them to
-- something openly editorial before you ship.

begin;

-- ---------------------------------------------------------------------
-- Cleanup of any previous run
-- ---------------------------------------------------------------------
delete from auth.users where id in ('00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003');

-- ---------------------------------------------------------------------
-- Seed accounts. The sign-up trigger turns each into a forum profile.
-- ---------------------------------------------------------------------
insert into auth.users (id, instance_id, aud, role, email, encrypted_password,
                        email_confirmed_at, created_at, updated_at,
                        raw_app_meta_data, raw_user_meta_data)
values
  ('00000000-0000-4000-8000-000000000001', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
   'seed.mehmet@electoolkit.invalid', '', now(), now() - interval '60 days', now(), '{}'::jsonb,
   jsonb_build_object('full_name', 'Mehmet Aydın')),
  ('00000000-0000-4000-8000-000000000002', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
   'seed.elif@electoolkit.invalid', '', now(), now() - interval '60 days', now(), '{}'::jsonb,
   jsonb_build_object('full_name', 'Elif Şahin')),
  ('00000000-0000-4000-8000-000000000003', '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
   'seed.ryan@electoolkit.invalid', '', now(), now() - interval '60 days', now(), '{}'::jsonb,
   jsonb_build_object('full_name', 'Ryan Whitfield'));

-- ---------------------------------------------------------------------
-- Categories. One row per language; the same subject is two rows.
-- ---------------------------------------------------------------------
insert into public.forum_categories (key, language, title, description, position) values
  ('installations', 'tr', 'Tesisat ve Montaj', 'Saha uygulaması, kablolama, buat ve pano işçiliği', 10),
  ('installations', 'en', 'Installations & Wiring', 'Field practice, cabling, enclosures and panel work', 10),
  ('protection', 'tr', 'Koruma ve Seçicilik', 'Şalterler, kaçak akım röleleri, koordinasyon', 20),
  ('protection', 'en', 'Protection & Selectivity', 'Breakers, RCDs and coordination', 20),
  ('troubleshooting', 'tr', 'Arıza Bulma', 'Atan şalter, ısınan iletken, ölçüm tutmuyor', 30),
  ('troubleshooting', 'en', 'Troubleshooting', 'Nuisance tripping, hot conductors, readings that will not add up', 30),
  ('design', 'tr', 'Proje ve Hesap', 'Kesit, gerilim düşümü, yük hesabı, pano tasarımı', 40),
  ('design', 'en', 'Design & Calculations', 'Sizing, voltage drop, load assessment, panel design', 40),
  ('standards', 'tr', 'Standartlar', 'IEC metinleri, ölçüm yöntemleri, muayene', 50),
  ('standards', 'en', 'Standards', 'IEC texts, test methods, inspection', 50),
  ('learning', 'tr', 'Öğrenme ve Meslek', 'Yeni başlayanlar, alet seçimi, kariyer', 60),
  ('learning', 'en', 'Learning & Trade', 'Getting started, tools, career', 60)
on conflict (key, language) do update
   set title = excluded.title, description = excluded.description,
       position = excluded.position, is_active = true;

-- ---------------------------------------------------------------------
-- Threads, opening posts and replies
-- ---------------------------------------------------------------------
do $$
declare
    cat uuid;
    thr uuid;
    op  uuid;
begin

    -- protection / tr
    select id into cat from public.forum_categories where key = 'protection' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'tr', 'C tipi şalter gereksiz yere atıyor, B tipine mi dönmeli?', now() - interval '26 days', now() - interval '26 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Atölyede 16 A C tipi şalter var, tezgâh motorunu her çalıştırdığımızda değil ama günde bir iki kez atıyor. Termik mi manyetik mi anlamanın pratik bir yolu var mı? B tipine geçmek mantıklı olur mu?', true, now() - interval '26 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Atma süresine bakın. Saniyeler içinde atıyorsa manyetik, birkaç dakika sonra atıyorsa termik. Motor kalkışında atıyorsa manyetiktir ve B tipine geçmek durumu kötüleştirir — B, anmasının 5 katında bırakır, C 10 katında. Ters yöne gitmiş olursunuz.', now() - interval '26 days' + interval '5 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Elif''e katılıyorum. Dakikalar sonra atıyorsa ya yük gerçekten anmaya yakındır ya da şalter sıcak bir pano içindedir. Hiçbir şeyi değiştirmeden önce çalışma akımını ölçmeye değer.', now() - interval '26 days' + interval '9 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Süreyi ölçtüm, birkaç dakika sonra atıyor. Termikmiş. Pano kapağı kapalıyken içerisi epey sıcak, oradan bakacağım. Teşekkürler.', now() - interval '26 days' + interval '30 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000002');

    -- protection / tr
    select id into cat from public.forum_categories where key = 'protection' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'tr', 'Kaçak akım rölesinin test butonu tam olarak neyi kanıtlıyor?', now() - interval '19 days', now() - interval '19 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Muayenede test butonuna basıp geçti yazan raporlar görüyorum. Bu buton mekanizmayı mı test ediyor yoksa tesisatı mı? Ayrı bir ölçüm şart mı?', true, now() - interval '19 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Yalnızca mekanizmayı. Buton cihazın içinde bir akım geçiriyor, topraklamanın bağlı olup olmadığı hakkında hiçbir şey söylemiyor. Açma süresini ayrıca ölçmek gerekir — IΔn ve 5×IΔn''de.', now() - interval '19 days' + interval '7 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Bir de beş kat testi, sadece yavaş olan cihazı yakalayan testtir. Bir cihaz anma kaçak akımında geçip, asıl önemli olan yerde hâlâ fazla yavaş olabilir.', now() - interval '19 days' + interval '14 hours');

    -- troubleshooting / tr
    select id into cat from public.forum_categories where key = 'troubleshooting' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'tr', 'Motor yol alırken bütün atölyede ışıklar kırpışıyor', now() - interval '23 days', now() - interval '23 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', '30 kW bir motor doğrudan yol veriliyor. Her kalkışta floresanlar gözle görülür kırpıyor. Trafo 400 kVA. Bu normal mi, yoksa bir sorun mu var?', true, now() - interval '23 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Trafo gücünü ve empedansını (uk) bilmek gerek. 400 kVA %4 ise kısa devre gücü 10 MVA; 30 kW motorun kalkış görünür gücü kabaca 230 kVA eder, çökme %2 civarı olur. Bu kırpışır ama sorun değildir.', now() - interval '23 days' + interval '4 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Kırpışma rahatsız ediyorsa yıldız-üçgen ya da yumuşak yol verici çözer. Ama torkun da gerilimin karesiyle düştüğünü unutmayın — yüklü kalkan bir makine hiç dönmeyebilir.', now() - interval '23 days' + interval '11 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Yumuşak yol vericiye geçtik, kırpışma bitti. Boşta kalkıyor zaten, tork sorun olmadı.', now() - interval '23 days' + interval '26 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- troubleshooting / tr
    select id into cat from public.forum_categories where key = 'troubleshooting' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'tr', 'Nötr iletkeni fazlardan daha sıcak, dengesizlik yok', now() - interval '15 days', now() - interval '15 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Ofis katında pano nötr barası fazlardan belirgin sıcak. Üç fazı da ölçtüm, akımlar birbirine yakın. Dengesizlik yokken nötr neden ısınsın?', true, now() - interval '15 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Yükler anahtarlamalı güç kaynağıysa üçün katı harmonikler nötrde toplanır, sönümlenmez. Dengeli bir kurulumda bile nötr hatlardan fazla akım taşıyabilir.', now() - interval '15 days' + interval '3 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Nötrü gerçek RMS ölçen bir pensle ölçüp bir fazla karşılaştırın. Bilgisayar dolu bir katta nötrün her fazdan belirgin fazla taşıdığını gördüm.', now() - interval '15 days' + interval '8 hours');

    -- design / tr
    select id into cat from public.forum_categories where key = 'design' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'tr', 'Uzun hatta kesit mi büyütmeli, gerilimi mi yükseltmeli?', now() - interval '30 days', now() - interval '30 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', '220 metrelik bir besleme var, gerilim düşümü sınırı zorluyor. Kesit büyütmek pahalı. Üç faza çıkarmak mantıklı bir alternatif mi?', true, now() - interval '30 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Üç faza çıkmak aynı gücü daha düşük akımla taşımak demek, düşüm doğrudan azalır. Kablo maliyeti de genelde tek fazda kesit büyütmekten iyi çıkar. Yükün üç fazlı olabilmesi şartıyla tabii.', now() - interval '30 days' + interval '6 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Bir de gerçekte neyin bağladığına bakın. Bu uzunlukta genelde düşüm değil toprak arıza çevrimi bağlar — büyük kesit ikisine de iyi gelir ama ucuz çözüm başka bir cihaz olabilir.', now() - interval '30 days' + interval '21 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Zs''yi hesaplayınca gerçekten çevrim bağlıyormuş. Kesit büyütmek yerine cihazı B tipine çevirmek yetti.', now() - interval '30 days' + interval '40 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- design / tr
    select id into cat from public.forum_categories where key = 'design' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'tr', 'Ortam sıcaklığı 45 °C, tablo değerlerini nasıl düzeltiyorsunuz?', now() - interval '12 days', now() - interval '12 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Kazan dairesinde ortam yazın 45 °C''yi buluyor. PVC kablo için düzeltme katsayısını uyguluyorum ama gruplama da var. İkisini çarpmak doğru mu?', true, now() - interval '12 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Evet, katsayılar çarpılır. 45 °C''de PVC için yaklaşık 0,79, altı devre gruplu ise 0,57 — ikisi birlikte 0,45 eder. Kablo kapasitesinin yarısından azına iniyorsunuz, bu ciddi bir fark.', now() - interval '12 days' + interval '5 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Bir de kablo o sıcaklığın içinden geçip geçmediğine bakın. Sadece bir metrelik bölümü sıcak bölgedeyse bütün hattı ona göre boyutlandırmak gereksiz pahalı olabilir.', now() - interval '12 days' + interval '16 hours');

    -- installations / tr
    select id into cat from public.forum_categories where key = 'installations' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'tr', 'Buat içinde klemens mi, geçmeli konnektör mü?', now() - interval '8 days', now() - interval '8 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Alışkanlıktan vidalı klemens kullanıyorum ama geçmeli konnektörler yaygınlaştı. Sahada gerçekten daha güvenilir mi, yoksa sadece hızlı mı?', true, now() - interval '8 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'İkisi de doğru uygulanırsa güvenilir. Fark şurada: vidalı klemens zamanla gevşer, özellikle alüminyumda ve ısınan noktalarda. Geçmelide o risk yok ama iletkeni tam dibe kadar itmek şart, yarım oturan bir tel ısınır.', now() - interval '8 days' + interval '4 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'En sık gördüğüm arıza, en baştan doğru sıkılmamış vidalı klemens. Hangisini kullanırsanız kullanın, belirleyen işçilik.', now() - interval '8 days' + interval '13 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000002');

    -- installations / tr
    select id into cat from public.forum_categories where key = 'installations' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'tr', 'Priz devresi için 2,5 mm² her zaman yeterli mi?', now() - interval '5 days', now() - interval '5 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Ev tesisatında priz devrelerine alışkanlıkla 2,5 mm² çekiyoruz. Uzun hatlarda veya çok priz olan devrelerde bu varsayım nerede bozulur?', true, now() - interval '5 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Uzunluk bozar. 2,5 mm² 16 A''de kapasite olarak rahat, ama 30 metreyi geçince gerilim düşümü sıkışmaya başlar. Bir de gruplama: aynı borudan altı devre geçiyorsa kapasite yarıya iner.', now() - interval '5 days' + interval '9 hours');

    -- standards / tr
    select id into cat from public.forum_categories where key = 'standards' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'tr', 'Ölçülen Zs hesaplanandan yüksek çıkarsa ne yaparsınız?', now() - interval '17 days', now() - interval '17 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Devrede ölçtüğüm çevrim empedansı, tasarımda hesaplananın belirgin üstünde. Cihaz limitinin altında yine de. Kabul edip geçiyor musunuz, yoksa peşine mi düşüyorsunuz?', true, now() - interval '17 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Peşine düşerim. Limitin altında olması güvenli demek, doğru monte edilmiş demek değil. Aradaki fark genelde gevşek bir klemens, beklenmedik bir ek ya da listedekinden ince bir kablodur.', now() - interval '17 days' + interval '6 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Aynı fikirdeyim. Hesaplanan 0,9 iken 1,4 ölçtüyseniz o 0,5 ohm bir yerden geliyor ve zamanla iyileşmez.', now() - interval '17 days' + interval '15 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- learning / tr
    select id into cat from public.forum_categories where key = 'learning' and language = 'tr';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'tr', 'Sahaya yeni başlayan biri ilk hangi aleti almalı?', now() - interval '2 days', now() - interval '2 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Yeni mezunum. Bütçe sınırlı. Pens ampermetre mi, çok fonksiyonlu test cihazı mı, yoksa iyi bir multimetre mi önce gelir?', true, now() - interval '2 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Önce iyi bir gerçek RMS multimetre ve iki kutuplu gerilim test kalemi. Test kalemi hayat kurtarır, multimetre her gün işe yarar. Çok fonksiyonlu cihaz pahalı ve muayene yapmıyorsan bekleyebilir.', now() - interval '2 days' + interval '3 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'İki kutuplu test kalemine ben de katılıyorum. Multimetre boşta duran bir iletkende size yalan söyleyebilir; iki kutuplu kalem devreyi yükler ve gerçeği gösterir.', now() - interval '2 days' + interval '7 hours');

    -- protection / en
    select id into cat from public.forum_categories where key = 'protection' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'en', 'Type C breaker nuisance tripping — is Type B the answer?', now() - interval '25 days', now() - interval '25 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', '16 A Type C on a small workshop, tripping once or twice a day but not obviously at motor start. Is there a practical way to tell thermal from magnetic before I change anything?', true, now() - interval '25 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Time it. Seconds means magnetic, minutes means thermal. If it is thermal, moving to Type B makes it worse — B lets go at five times its rating, C at ten.', now() - interval '25 days' + interval '6 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Also check the enclosure temperature. A breaker rated at 30 °C sitting in a closed panel at 45 °C is derated whether the datasheet is on the wall or not.', now() - interval '25 days' + interval '18 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- protection / en
    select id into cat from public.forum_categories where key = 'protection' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'en', 'What does an RCD test button actually prove?', now() - interval '20 days', now() - interval '20 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'I keep seeing inspection reports where the only evidence is that the test button worked. Does that button test the installation or only the device?', true, now() - interval '20 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Only the device. It injects a current inside the RCD and says nothing about whether the earth is connected. You still need to measure the operating time at IΔn and at five times it.', now() - interval '20 days' + interval '8 hours');

    -- troubleshooting / en
    select id into cat from public.forum_categories where key = 'troubleshooting' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'en', 'Lights flicker every time a 30 kW motor starts', now() - interval '22 days', now() - interval '22 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Direct on line start, 400 kVA transformer. The fluorescents visibly dip on every start. Is this expected or is something wrong?', true, now() - interval '22 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Depends on the transformer impedance. 400 kVA at 4 % is a 10 MVA source; a 30 kW motor pulls roughly 230 kVA starting, which is about a 2 % dip. Visible, not a fault.', now() - interval '22 days' + interval '5 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'If it bothers people, star-delta or a soft starter fixes it — but remember torque falls with the square of voltage. A loaded machine may not turn at all.', now() - interval '22 days' + interval '12 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- troubleshooting / en
    select id into cat from public.forum_categories where key = 'troubleshooting' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'en', 'Neutral running hotter than the lines, and the load is balanced', now() - interval '14 days', now() - interval '14 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Office floor, neutral bar noticeably warmer than the phases. All three lines measure within a couple of amps of each other. What am I missing?', true, now() - interval '14 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Triplen harmonics. On a floor of switch-mode supplies the third and ninth arrive at the star point in phase and add instead of cancelling. A balanced board can still cook its neutral.', now() - interval '14 days' + interval '4 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Clamp the neutral with a true RMS meter and compare. I have measured a neutral carrying more than any single line on exactly this kind of floor.', now() - interval '14 days' + interval '10 hours');

    -- design / en
    select id into cat from public.forum_categories where key = 'design' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'en', 'Long run: increase the conductor or move to three phase?', now() - interval '28 days', now() - interval '28 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', '220 m feeder, voltage drop is the binding constraint. Upsizing the cable is expensive. Is going three phase a sensible alternative?', true, now() - interval '28 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Three phase carries the same power at lower current, so the drop falls directly, and the cable usually costs less than upsizing single phase. Assuming the load can be three phase.', now() - interval '28 days' + interval '7 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Check what is actually binding first. On a run that long the earth fault loop often bites before the drop does, and that is a different fix.', now() - interval '28 days' + interval '19 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000001');

    -- design / en
    select id into cat from public.forum_categories where key = 'design' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'en', 'Ambient is 45 °C — do you multiply the correction factors?', now() - interval '11 days', now() - interval '11 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Plant room hits 45 °C in summer, and the cables are grouped. Is it correct to multiply the ambient factor by the grouping factor, or is that double counting?', true, now() - interval '11 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Multiply them. They describe two independent things: how well the cable sheds heat to the air, and how much of that air is already warmed by its neighbours.', now() - interval '11 days' + interval '6 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Worth checking how much of the run is actually in the hot room. Sizing 60 m for a 2 m hot section is expensive.', now() - interval '11 days' + interval '20 hours');

    -- installations / en
    select id into cat from public.forum_categories where key = 'installations' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'en', 'Screw terminals or push-in connectors in junction boxes?', now() - interval '9 days', now() - interval '9 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Habit says screw terminals. Push-in connectors are everywhere now. Are they genuinely more reliable in the field, or just faster?', true, now() - interval '9 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'Both are fine done properly. Screw terminals loosen over time, especially on aluminium and anywhere that heats. Push-in avoids that, but the conductor must be fully seated — a half-inserted strand runs hot.', now() - interval '9 days' + interval '5 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000002');

    -- installations / en
    select id into cat from public.forum_categories where key = 'installations' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000002', 'en', 'Is 2.5 mm² always enough for a socket circuit?', now() - interval '6 days', now() - interval '6 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'We default to 2.5 mm² on domestic socket circuits. Where does that assumption actually break down?', true, now() - interval '6 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Length breaks it. 2.5 mm² is comfortable for 16 A on capacity, but past about 30 m the drop starts to bind. Grouping breaks it too — six circuits in one conduit halves the capacity.', now() - interval '6 days' + interval '11 hours');

    -- standards / en
    select id into cat from public.forum_categories where key = 'standards' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000001', 'en', 'Measured Zs higher than calculated — do you chase it?', now() - interval '16 days', now() - interval '16 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'The loop impedance I measure is well above what the design predicted, but still inside the device maximum. Do you accept it or investigate?', true, now() - interval '16 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Investigate. Inside the limit means safe, not correct. That gap is usually a loose termination, an unexpected joint, or a cable that is not what the schedule says.', now() - interval '16 days' + interval '7 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'And it does not improve on its own. A joint that adds half an ohm today adds more when it warms up.', now() - interval '16 days' + interval '17 hours');
    -- Never from the author: self-thanks is refused by policy, and seeding
    -- around it would put data in the table the app could never create.
    insert into public.forum_thanks (post_id, user_id) values (op, '00000000-0000-4000-8000-000000000002');

    -- learning / en
    select id into cat from public.forum_categories where key = 'learning' and language = 'en';
    insert into public.forum_threads (category_id, author_id, language, title, created_at, last_reply_at)
    values (cat, '00000000-0000-4000-8000-000000000003', 'en', 'First instrument for someone new to the trade?', now() - interval '3 days', now() - interval '3 days')
    returning id into thr;
    insert into public.forum_posts (thread_id, author_id, body, is_opening_post, created_at)
    values (thr, '00000000-0000-4000-8000-000000000003', 'Newly qualified, limited budget. Clamp meter, multifunction tester, or a good multimeter first?', true, now() - interval '3 days')
    returning id into op;
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000002', 'A good true RMS multimeter and a two-pole voltage tester. The tester keeps you safe, the multimeter earns its keep daily. A multifunction tester can wait unless you are doing inspections.', now() - interval '3 days' + interval '4 hours');
    insert into public.forum_posts (thread_id, author_id, body, created_at)
    values (thr, '00000000-0000-4000-8000-000000000001', 'Two-pole tester, definitely. A high-impedance multimeter will show you a phantom voltage on a floating conductor and you will chase it for an hour.', now() - interval '3 days' + interval '9 hours');
end $$;

commit;

-- To remove everything this script created:
--   delete from auth.users where id in (
--     '00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000003');
-- Categories are left in place deliberately — they are yours, not seed data.
