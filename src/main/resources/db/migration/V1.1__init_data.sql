SET client_encoding = 'UTF8';

COPY film (id, release_date, synopsis, title) FROM stdin;
1	2017-10-10	Imprisoned on the planet Sakaar, Thor must race against time to return to Asgard and stop Ragnarök, the destruction of his world, at the hands of the powerful and ruthless  villain Hela.	Thor: Ragnarok
2	2018-04-23	The Avengers and their allies must be willing to sacrifice all in an attempt to defeat the powerful Thanos before his blitz of devastation and ruin puts an end to the universe.	Avengers: Infinity War
3	2019-09-08	A young German boy in the Hitler Youth whose hero and imaginary friend is the country's dictator is shocked to discover that his mother is hiding a Jewish girl in their home.	Jojo Rabbit
\.

COPY person (id, dob, name) FROM stdin;
1	1984-11-22	Scarlett Johansson
2	1967-11-22	Mark Ruffalo
3	1975-08-16	Taika Waititi
4	1971-07-08	Joe Russo
5	1970-02-03	Anthony Russo
\.

COPY film_person_directed (film_id, person_id) FROM stdin;
1	3
2	5
2	4
3	3
\.

COPY role (film_id, person_id, "character") FROM stdin;
2	1	Natasha Romanoff, Black Widow
2	2	Bruce Banner, Hulk
1	2	Bruce Banner, Hulk
3	3	Adolf
3	1	Rosie
\.

SELECT pg_catalog.setval('film_id_seq', 3, true);

SELECT pg_catalog.setval('person_id_seq', 5, true);
