INSERT INTO elections (name, description, starts_at, ends_at)
VALUES ('Wybory samorządowe 2023 (jednodniowe)',
        'Jednodniowe wybory, które odbyły się 1 stycznia 2023',
        '2023-01-01 08:00:00',
        '2023-01-01 20:00:00');

INSERT INTO elections (name, description, starts_at, ends_at)
VALUES ('Plebiscyt Grudzień 2025',
        'Głosowanie trwające cały grudzień 2025',
        '2025-12-01 00:00:00',
        '2025-12-31 23:59:59');

INSERT INTO elections (name, description, starts_at, ends_at)
VALUES ('Wybory samorządowe 2027 (jednodniowe)',
        'Jednodniowe wybory zaplanowane na 1 stycznia 2027',
        '2027-01-01 08:00:00',
        '2027-01-01 20:00:00');

-- Past election options
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat A 2023' FROM elections WHERE name = 'Wybory samorządowe 2023 (jednodniowe)';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat B 2023' FROM elections WHERE name = 'Wybory samorządowe 2023 (jednodniowe)';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat C 2023' FROM elections WHERE name = 'Wybory samorządowe 2023 (jednodniowe)';

-- Ongoing December 2025 options
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat A 2025' FROM elections WHERE name = 'Plebiscyt Grudzień 2025';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat B 2025' FROM elections WHERE name = 'Plebiscyt Grudzień 2025';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat C 2025' FROM elections WHERE name = 'Plebiscyt Grudzień 2025';

-- Future 2027 options
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat A 2027' FROM elections WHERE name = 'Wybory samorządowe 2027 (jednodniowe)';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat B 2027' FROM elections WHERE name = 'Wybory samorządowe 2027 (jednodniowe)';
INSERT INTO election_options (election_id, label)
SELECT id, 'Kandydat C 2027' FROM elections WHERE name = 'Wybory samorządowe 2027 (jednodniowe)';
