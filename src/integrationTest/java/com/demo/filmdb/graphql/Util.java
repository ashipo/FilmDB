package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.graphql.inputs.FilmInput;
import com.demo.filmdb.person.Person;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

class Util {
    static final String FILMS = "films";
    static final String FILM_BY_ID = "filmById";
    static final String DELETE_FILM = "deleteFilm";
    static final String CREATE_FILM = "createFilm";
    static final String UPDATE_FILM = "updateFilm";
    static final String UPDATE_DIRECTORS = "updateFilmDirectors";
    static final String CREATE_ROLE = "createRole";

    public static final String DATA = "data";

    static FilmInput getValidFilmInput() {
        return new FilmInput("Mission: Impossible", LocalDate.now(), "There is a mission.");
    }

    static Map<String, Object> getFilmInputMap(FilmInput input) {
        return new HashMap<>() {{
            put("title", input.title());
            put("releaseDate", input.releaseDate());
            put("synopsis", input.synopsis());
        }};
    }

    private static Film createFilmWithoutId() {
        return new Film("Terminator", LocalDate.of(1984, 10, 26), "A human soldier is sent from 2029 to 1984");
    }

    static Film createFilm(Long id) {
        Film result = createFilmWithoutId();
        result.setId(id);
        return result;
    }

    private static Person createPersonWithoutId() {
        return new Person("Arnold Schwarzenegger", LocalDate.of(1947, 7, 30));
    }

    static Person createPerson(Long id) {
        Person result = createPersonWithoutId();
        result.setId(id);
        return result;
    }
}
