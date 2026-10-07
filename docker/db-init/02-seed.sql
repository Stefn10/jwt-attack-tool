INSERT INTO categories (name, description) VALUES
    ('Sport',       'Najnovije sportske vesti, prenosi meceva, intervjui sa sportistima i analize.'),
    ('Tehnologija', 'Tehnoloske vesti, recenzije gadgeta, inovacije i vestacka inteligencija.'),
    ('Kultura',     'Filmovi, muzika, knjige, izlozbe i kulturni dogadjaji u zemlji i svetu.');

INSERT INTO users (email, name, surname, user_type, status, password) VALUES
    ('petar@raf.rs',  'Petar',  'Petrovic',  'CREATOR', TRUE,
     '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'),
    ('marko@raf.rs',  'Marko',  'Markovic',  'CREATOR', TRUE,
     '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'),
    ('jelena@raf.rs', 'Jelena', 'Jovanovic', 'CREATOR', FALSE,  -- deactivated account for testing
     '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');

INSERT INTO news (title, text, published_time, number_of_visits, author_id, category_id) VALUES

    ('Djokovic osvojio Australian Open',
     'Novak Djokovic je u dramaticnom finalu pobedio Karlosa Alkaraza rezultatom 3:2 i osvojio svoj jedanaesti pehar u Melburnu. Mec je trajao gotovo pet sati a publika je bila na nogama do poslednje lopte.',
     CURRENT_TIMESTAMP - INTERVAL '2 days', 0, 2, 1),

    ('Crvena Zvezda u finalu Evrolige',
     'Kosarkasi Crvene Zvezde plasirali su se u finale Evrolige posle senzacionalne pobede nad Real Madridom. Najefikasniji u redovima crveno-belih bio je kapiten sa 28 poena.',
     CURRENT_TIMESTAMP - INTERVAL '5 days', 0, 3, 1),


    ('OpenAI predstavio GPT-6',
     'OpenAI je danas zvanicno predstavio novi GPT-6 model koji prema internim testovima nadmasuje sve prethodne verzije u zadacima rezonovanja, pisanja koda i razumevanja konteksta. Dostupan je za sve Plus korisnike.',
     CURRENT_TIMESTAMP - INTERVAL '10 days', 0, 2, 2),

    ('Apple iPhone 17 stigao u prodaju',
     'Apple je danas zvanicno pustio u prodaju novi iPhone 17 sa unapredjenim A19 cipom, boljom kamerom i dramaticnim redizajnom kucista. Cene pocinju od 999 dolara za osnovni model.',
     CURRENT_TIMESTAMP - INTERVAL '1 day', 0, 1, 2),


    ('Festival u Kanu zatvorio vrata',
     'Sedamdeset deveto izdanje Filmskog festivala u Kanu zvanicno je zatvoreno dodelom Zlatne palme. Pobednicki film "Tisina nakon oluje" rezisera Pjera Lefevra dobio je ovacije publike.',
     CURRENT_TIMESTAMP - INTERVAL '15 days', 0, 3, 3),

    ('Nova knjiga Harukija Murakamija',
     'Japanski pisac Haruki Murakami objavio je nov roman pod nazivom "Grad mecaka i njegova zid". Knjiga je vec tri nedelje na vrhu lista prodaje u Japanu, a prevodi se ocekuju do kraja godine.',
     CURRENT_TIMESTAMP - INTERVAL '40 days', 0, 2, 3);  -- older than 30 days (will not appear in "most read")

INSERT INTO tags (value) VALUES
    ('tenis'),
    ('djokovic'),
    ('kosarka'),
    ('evroliga'),
    ('ai'),
    ('openai'),
    ('apple'),
    ('iphone'),
    ('film'),
    ('festival'),
    ('knjiga'),
    ('murakami');

INSERT INTO news_tags (news_id, tag_id) VALUES

    (1, 1), (1, 2),

    (2, 3), (2, 4),

    (3, 5), (3, 6),

    (4, 7), (4, 8),

    (5, 9), (5, 10),

    (6, 11), (6, 12);

INSERT INTO news_visits (news_id, session_id, visited_at) VALUES
    (1, 'session-alice', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (1, 'session-bob',   CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (1, 'session-carol', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
    (3, 'session-alice', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (3, 'session-bob',   CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (4, 'session-alice', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
    (4, 'session-bob',   CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    (4, 'session-carol', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
    (4, 'session-dave',  CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
    (2, 'session-alice', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (5, 'session-bob',   CURRENT_TIMESTAMP - INTERVAL '14 days');

UPDATE news SET number_of_visits = (
    SELECT COUNT(*) FROM news_visits WHERE news_visits.news_id = news.news_id
);

INSERT INTO news_reactions (news_id, session_id, reaction_type) VALUES
    (1, 'session-alice', 'LIKE'),
    (1, 'session-bob',   'LIKE'),
    (1, 'session-carol', 'DISLIKE'),
    (3, 'session-alice', 'LIKE'),
    (3, 'session-bob',   'LIKE'),
    (4, 'session-alice', 'LIKE'),
    (4, 'session-bob',   'DISLIKE'),
    (4, 'session-carol', 'LIKE'),
    (2, 'session-dave',  'LIKE');

INSERT INTO comments (news_id, author_name, text, created_at) VALUES
    (1, 'Marko iz Beograda',  'Bravo Nole, najveci svih vremena!',                   CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (1, 'Ana',                'Kakav mec, gledala sam do tri ujutru.',               CURRENT_TIMESTAMP - INTERVAL '20 hours'),
    (1, 'Aleksandar',         'Alkaraz ce mu se osvetiti u Parizu.',                 CURRENT_TIMESTAMP - INTERVAL '5 hours'),
    (3, 'devjs',              'GPT-6 izgleda zaista impresivno na demoima.',         CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (3, 'skepticno',          'Verujem tek kad probam, marketing je uvek prejak.',   CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (4, 'Tehno fan',          'Cena je strasna, ali iPhone je iPhone.',              CURRENT_TIMESTAMP - INTERVAL '1 hour');

INSERT INTO comment_reactions (comment_id, session_id, reaction_type) VALUES
    (1, 'session-bob',   'LIKE'),
    (1, 'session-carol', 'LIKE'),
    (2, 'session-alice', 'LIKE'),
    (4, 'session-carol', 'LIKE'),
    (5, 'session-bob',   'DISLIKE'),
    (6, 'session-alice', 'DISLIKE');

INSERT INTO tags (value) VALUES
                             ('sport'),
                             ('tehnologija'),
                             ('kultura');

INSERT INTO news_tags (news_id, tag_id) VALUES
                                            (1, 13), (2, 13),
                                            (3, 14), (4, 14),
                                            (5, 15), (6, 15);