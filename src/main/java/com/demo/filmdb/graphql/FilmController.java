package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller("graphqlFilmController")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @QueryMapping
    public Film filmById(@Argument String id) {
        return filmService.getFilm(Long.parseLong(id));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public long deleteFilm(@Argument String id) {
        long parsedId = Long.parseLong(id);
        filmService.deleteFilmById(parsedId);
        return parsedId;
    }
}
