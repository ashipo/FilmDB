package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Util {
    // documents
    static final String FILMS = "films";
    static final String GET_FILM = "film";
    static final String DELETE_FILM = "deleteFilm";
    static final String CREATE_FILM = "createFilm";
    static final String UPDATE_FILM = "updateFilm";
    static final String SET_DIRECTOR = "setDirector";
    static final String DELETE_DIRECTOR = "deleteDirector";
    static final String UPDATE_DIRECTORS = "updateDirectors";
    static final String CREATE_ROLE = "createRole";
    static final String GET_ROLE = "role";
    static final String UPDATE_ROLE = "updateRole";
    static final String DELETE_ROLE = "deleteRole";
    static final String UPDATE_CAST = "updateCast";
    static final String CREATE_PERSON = "createPerson";
    static final String GET_PERSON = "person";
    static final String UPDATE_PERSON = "updatePerson";
    static final String DELETE_PERSON = "deletePerson";
    // variables
    static final String VAR_ID = "id";
    static final String FILM_ID = "filmId";
    static final String PERSON_ID = "personId";
    static final String CHARACTER = "character";
    static final String NAME = "name";
    static final String DATE_OF_BIRTH = "dateOfBirth";
    static final String TITLE = "title";
    static final String RELEASE_DATE = "releaseDate";
    static final String SYNOPSIS = "synopsis";
    // named arguments
    static final Named<Object> NULL = named("[Null]", null);
    static final Named<String> EMPTY_STRING = named("[Empty]", "");
    static final Named<String> BLANK_STRING = named("[Blank]", "  ");
    static final Named<String> INVALID_DATE = named("[Invalid]", "2000-20-20");

    public static final String DATA = "data";

    static Film createFilm(Long id, String title, LocalDate releaseDate, @Nullable String synopsis) {
        Film result = new Film(title, releaseDate, synopsis);
        result.setId(id);
        return result;
    }

    static Film createFilm(Long id) {
        return createFilm(id, "Terminator", LocalDate.of(1984, 10, 26), "A human soldier is sent from 2029 to 1984");
    }

    static Person createPerson(Long id) {
        return createPerson(id, "Arnold Schwarzenegger", LocalDate.of(1947, 7, 30));
    }

    static Person createPerson(Long id, String name, @Nullable LocalDate dateOfBirth) {
        Person result = new Person(name, dateOfBirth);
        result.setId(id);
        return result;
    }

    static Role createRole(Long filmId, Long personId) {
        return new Role(createFilm(filmId), createPerson(personId), "Sarah Connor");
    }

    static Role createRole(Long filmId, Long personId, String character) {
        return new Role(createFilm(filmId), createPerson(personId), character);
    }

    static Stream<Arguments> invalidCrewMemberIdInputs() {
        return Stream.of(
                arguments(null, 1L),
                arguments(1L, null),
                arguments("A", 1L),
                arguments(1L, "A")
        );
    }
}
