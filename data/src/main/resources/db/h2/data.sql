INSERT INTO film VALUES(default, DATE '2017-10-10', 'Imprisoned on the planet Sakaar, Thor must race against time to return to Asgard and stop Ragnarök, the destruction of his world, at the hands of the powerful and ruthless  villain Hela.', 'Thor: Ragnarok');
INSERT INTO film VALUES(default, DATE '2018-04-23', 'The Avengers and their allies must be willing to sacrifice all in an attempt to defeat the powerful Thanos before his blitz of devastation and ruin puts an end to the universe.', 'Avengers: Infinity War');
INSERT INTO film VALUES(default, DATE '2019-09-08', 'A young German boy in the Hitler Youth whose hero and imaginary friend is the country''s dictator is shocked to discover that his mother is hiding a Jewish girl in their home.', 'Jojo Rabbit');

INSERT INTO person VALUES(default, DATE '1984-11-22', 'Scarlett Johansson');
INSERT INTO person VALUES(default, DATE '1967-11-22', 'Mark Ruffalo');
INSERT INTO person VALUES(default, DATE '1975-08-16', 'Taika Waititi');
INSERT INTO person VALUES(default, DATE '1971-07-08', 'Joe Russo');
INSERT INTO person VALUES(default, DATE '1970-02-03', 'Anthony Russo');

INSERT INTO film_person_directed VALUES(1, 3);
INSERT INTO film_person_directed VALUES(2, 5);
INSERT INTO film_person_directed VALUES(2, 4);
INSERT INTO film_person_directed VALUES(3, 3);

INSERT INTO role VALUES(2, 1, 'Natasha Romanoff, Black Widow');
INSERT INTO role VALUES(2, 2, 'Bruce Banner, Hulk');
INSERT INTO role VALUES(1, 2, 'Bruce Banner, Hulk');
INSERT INTO role VALUES(3, 3, 'Adolf');
INSERT INTO role VALUES(3, 1, 'Rosie');