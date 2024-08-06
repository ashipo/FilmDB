package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.inputs.FilmInput;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller("graphqlFilmController")
public class FilmController {

    private final FilmService filmService;

    public FilmController(
            FilmService filmService
    ) {
        this.filmService = filmService;
    }

    @QueryMapping
    public Iterable<Film> films(@Argument int page, @Argument int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return filmService.getAllFilms(pageable);
    }

    @QueryMapping
    public @Nullable Film film(@Argument Long id) {
        return filmService.getFilm(id).orElse(null);
    }

    @MutationMapping
    public Long deleteFilm(@Argument Long id) {
        filmService.deleteFilm(id);
        return id;
    }

    @MutationMapping
    public Film createFilm(@Argument FilmInput input) {
        Film film = new Film();
        updateFilmFromInput(film, input);
        return filmService.saveFilm(film);
    }

    @MutationMapping
    public Film updateFilm(@Argument Long id, @Argument FilmInput input) {
        final Film film = new Film();
        film.setId(id);
        updateFilmFromInput(film, input);
        return filmService.updateFilm(film);
    }

    @MutationMapping
    public Film updateFilmDirectors(@Argument Long filmId, @Argument List<Long> directorsIds) {
        return filmService.updateDirectors(filmId, directorsIds);
    }

    private void updateFilmFromInput(Film film, FilmInput input) {
        film.setTitle(input.title());
        film.setReleaseDate(input.releaseDate());
        film.setSynopsis(input.synopsis());
    }
}
