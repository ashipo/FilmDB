package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.exceptions.EntityNotFoundException;
import com.demo.filmdb.graphql.inputs.FilmInput;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller("graphqlFilmController")
public class FilmController {

    private final FilmService filmService;
    private final PersonService personService;

    public FilmController(
            FilmService filmService,
            PersonService personService
    ) {
        this.filmService = filmService;
        this.personService = personService;
    }

    @QueryMapping
    public Iterable<Film> films(@Argument int page, @Argument int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return filmService.getAllFilms(pageable);
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
        Film film = new Film();
        updateFilmFromInput(film, input);
        return filmService.saveFilm(film);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Film updateFilm(@Argument Long id, @Argument FilmInput input) {
        final Film film = filmService.getFilm(id);
        if (film == null) {
            return null;
        }
        updateFilmFromInput(film, input);
        return filmService.saveFilm(film);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Film updateFilmDirectors(@Argument Long filmId, @Argument List<Long> directorsIds) {
        final Film film = filmService.getFilm(filmId);
        if (film == null) {
            return null;
        }
        if (directorsIds == null || directorsIds.isEmpty()) {
            return filmService.deleteDirectors(film);
        }
        List<Person> directors = personService.getPeople(directorsIds);
        if (directors.size() != directorsIds.size()) {
            List<Long> notFoundIds = notFoundIds(directorsIds, directors);
            throw new EntityNotFoundException("People with ids " + notFoundIds + " could not be found.");
        }
        return filmService.updateDirectors(film, directors);
    }

    private void updateFilmFromInput(Film film, FilmInput input) {
        film.setTitle(input.title());
        film.setReleaseDate(input.releaseDate());
        film.setSynopsis(input.synopsis());
    }

    private List<Long> notFoundIds(List<Long> requestedIds, List<Person> found) {
        Set<Long> existingIds = found.stream().map(Person::getId).collect(Collectors.toSet());
        return requestedIds.stream().filter(Predicate.not(existingIds::contains)).toList();
    }
}
