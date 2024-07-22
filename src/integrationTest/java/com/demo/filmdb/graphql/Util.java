package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.FilmInput;

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
}
