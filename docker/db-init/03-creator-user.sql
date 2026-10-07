-- Demo CREATOR user for the BVS attack-tool demo.
-- this user was previously added by hand to the local database; here it is part of the init script
-- so a from-scratch demo works with no extra steps.
-- Password = sha256("admin") = 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918

INSERT INTO users (email, name, surname, user_type, status, password)
VALUES ('creator@raf.rs', 'Demo', 'Creator', 'CREATOR', TRUE,
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918')
ON CONFLICT (email) DO NOTHING;
