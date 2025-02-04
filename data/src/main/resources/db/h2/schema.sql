DROP TABLE film IF EXISTS CASCADE CONSTRAINTS;
DROP TABLE person IF EXISTS CASCADE CONSTRAINTS;
DROP TABLE role IF EXISTS CASCADE CONSTRAINTS;
DROP TABLE film_person_directed IF EXISTS CASCADE CONSTRAINTS;

CREATE TABLE film(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    release_date DATE NOT NULL,
    synopsis CHARACTER VARYING(2000),
    title CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE person(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    date_of_birth DATE,
    name CHARACTER VARYING(255) NOT NULL
);

CREATE TABLE film_person_directed(
    film_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL
);

CREATE TABLE role(
    film_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    "CHARACTER" CHARACTER VARYING(255) NOT NULL
);

ALTER TABLE film_person_directed ADD CONSTRAINT pk_directed PRIMARY KEY(film_id, person_id);
ALTER TABLE film_person_directed ADD CONSTRAINT fk_directed_film FOREIGN KEY(film_id) REFERENCES film (id);
ALTER TABLE film_person_directed ADD CONSTRAINT fk_directed_person FOREIGN KEY(person_id) REFERENCES person (id);

ALTER TABLE role ADD CONSTRAINT pk_role PRIMARY KEY(film_id, person_id);
ALTER TABLE role ADD CONSTRAINT fk_role_film FOREIGN KEY(film_id) REFERENCES film (id);
ALTER TABLE role ADD CONSTRAINT fk_role_person FOREIGN KEY(person_id) REFERENCES person (id);