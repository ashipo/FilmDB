package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.inputs.CreateFilmInput;
import com.demo.filmdb.graphql.inputs.DeleteFilmInput;
import com.demo.filmdb.graphql.inputs.UpdateFilmInput;
import com.demo.filmdb.graphql.payloads.CreateFilmPayload;
import com.demo.filmdb.graphql.payloads.DeleteFilmPayload;
import com.demo.filmdb.graphql.payloads.UpdateFilmPayload;
import com.demo.filmdb.utils.SortUtil.SortableFilmField;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller("graphqlFilmController")
public class FilmController {

    private final FilmService filmService;

    public FilmController(
            FilmService filmService
    ) {
        this.filmService = filmService;
    }

    @QueryMapping
    public Iterable<Film> films(
            @Argument int page,
            @Argument int pageSize,
            @Argument @Nullable SortableFilmField sortBy,
            @Argument @Nullable Sort.Direction sortDirection,
            @Argument @Nullable String title,
            @Argument @Nullable LocalDate releaseAfter,
            @Argument @Nullable LocalDate releaseBefore
    ) {
        String sortByField = sortBy == null ? null : sortBy.getFieldName();
        return filmService.getFilms(page, pageSize, sortByField, sortDirection, title, releaseAfter, releaseBefore);
    }

    @QueryMapping
    public @Nullable Film film(@Argument Long id) {
        return filmService.getFilm(id).orElse(null);
    }

    @MutationMapping
    public CreateFilmPayload createFilm(@Argument CreateFilmInput input) {
        final Film createdFilm = filmService.createFilm(input.filmInput());
        return new CreateFilmPayload(createdFilm);
    }

    @MutationMapping
    public UpdateFilmPayload updateFilm(@Argument UpdateFilmInput input) {
        Film updatedFilm = filmService.updateFilm(input.id(), input.filmInput());
        return new UpdateFilmPayload(updatedFilm);
    }

    @MutationMapping
    public DeleteFilmPayload deleteFilm(@Argument DeleteFilmInput input) {
        final Long filmId = input.id();
        filmService.deleteFilm(filmId);
        return new DeleteFilmPayload(filmId);
    }
}
