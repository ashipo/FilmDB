package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.inputs.FilmInput;
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
    public Film filmById(@Argument Long id) {
        return filmService.getFilm(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long deleteFilm(@Argument Long id) {
        filmService.deleteFilmById(id);
        return id;
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Film createFilm(@Argument FilmInput input) {
        Film film = new Film(input.title(), input.releaseDate(), input.synopsis());
        return filmService.saveFilm(film);
    }
}
