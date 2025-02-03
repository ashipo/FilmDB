package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class Util {
    // documents
    static public final String FILMS = "films";
    static public final String GET_FILM = "film";
    static public final String DELETE_FILM = "deleteFilm";
    static public final String CREATE_FILM = "createFilm";
    static public final String UPDATE_FILM = "updateFilm";
    static public final String SET_DIRECTOR = "setDirector";
    static public final String DELETE_DIRECTOR = "deleteDirector";
    static public final String UPDATE_DIRECTORS = "updateDirectors";
    static public final String CREATE_ROLE = "createRole";
    static public final String GET_ROLE = "role";
    static public final String UPDATE_ROLE = "updateRole";
    static public final String DELETE_ROLE = "deleteRole";
    static public final String UPDATE_CAST = "updateCast";
    static public final String PEOPLE = "people";
    static public final String CREATE_PERSON = "createPerson";
    static public final String GET_PERSON = "person";
    static public final String UPDATE_PERSON = "updatePerson";
    static public final String DELETE_PERSON = "deletePerson";
    // variables
    static public final String VAR_ID = "id";
    static public final String FILM_ID = "filmId";
    static public final String PERSON_ID = "personId";
    static public final String CHARACTER = "character";
    static public final String NAME = "name";
    static public final String DATE_OF_BIRTH = "dateOfBirth";
    static public final String TITLE = "title";
    static public final String RELEASE_DATE = "releaseDate";
    static public final String SYNOPSIS = "synopsis";
    static public final String DIRECTORS_IDS = "directorsIds";
    static public final String CAST = "cast";
    static public final String PAGE = "page";
    static public final String PAGE_SIZE = "pageSize";
    static public final String SORT_BY = "sortBy";
    static public final String SORT_DIRECTION = "sortDirection";
    // named arguments
    static public final Named<Object> NULL = named("[Null]", null);
    static public final Named<String> EMPTY_STRING = named("[Empty]", "");
    static public final Named<String> BLANK_STRING = named("[Blank]", "  ");
    static public final Named<String> INVALID_DATE = named("[Invalid]", "2000-20-20");

    static public Named<String> stringOfLength(int len) {
        return named("[Length = " + len + "]", "A".repeat(len));
    }

    static public final String DATA = "data";

    static public Film createFilm(Long id) {
        return new Film(id, "Terminator", LocalDate.of(1984, 10, 26), "A human soldier is sent from 2029 to 1984");
    }

    static Person createPerson(Long id) {
        return new Person(id, "Arnold Schwarzenegger", LocalDate.of(1947, 7, 30));
    }

    static Role createRole(Long filmId, Long personId) {
        return new Role(createFilm(filmId), createPerson(personId), "Sarah Connor");
    }

    static Role createRole(Long filmId, Long personId, String character) {
        return new Role(createFilm(filmId), createPerson(personId), character);
    }

    static Stream<Arguments> invalidCrewMemberIdInputs() {
        return Stream.of(
                arguments(NULL, 1L),
                arguments(1L, NULL),
                arguments("A", 1L),
                arguments(1L, "A")
        );
    }
}
