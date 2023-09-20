package com.demo.filmdb.film;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository, RoleRepository roleRepository) {
        this.filmRepository = filmRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Returns a {@link Page} of {@link Film} entities matching the given {@link Specification}.
     * @param spec must not be {@code null}.
     * @param pageable must not be {@code null}.
     * @return a page of filtered entities.
     */
    public Page<Film> search(Specification<Film> spec, Pageable pageable) {
        return filmRepository.findAll(spec, pageable);
    }

    /**
     * Returns a {@link Page} of all {@link Film} entities.
     * @param pageable must not be {@code null}.
     * @return a page.
     */
    public Page<Film> getAllFilms(Pageable pageable) {
        return filmRepository.findAll(pageable);
    }

    /**
     * Saves a given {@link Film} entity.
     * @param film must not be {@code null}.
     * @return the saved entity.
     */
    public Film saveFilm(Film film) {
        return filmRepository.save(film);
    }

    /**
     * Returns a {@link Film} entity with the given {@code filmId} or throws 404 if it doesn't exist.
     * @param filmId must not be {@code null}.
     * @return the found entity.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    public Film getFilm(Long filmId) {
        return filmRepository.findById(filmId).orElseThrow(() -> getNotFoundException(filmId));
    }

    /**
     * Deletes a {@link Film} entity with the given {@code filmId} or throws 404 if it doesn't exist.
     * @param filmId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    @Transactional
    public void deleteFilm(Long filmId) {
        assertFilmExists(filmId);
        roleRepository.deleteById_FilmId(filmId);
        filmRepository.deleteById(filmId);
    }

    /**
     * Returns a collection of {@link Person} entities containing directors for a {@link Film} with the given
     * {@code filmId}
     * @param filmId must not be {@code null}.
     * @return the directors.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    Set<Person> getDirectors(Long filmId) {
        return getFilm(filmId).getDirectors();
    }

    /**
     * Replaces directors for the {@link Film}.
     * @param filmId must not be {@code null}.
     * @param directors a collection of {@link Person} entities to set as directors for the {@link Film}
     *                 with the given {@code filmId}. Set to {@code null} to empty the directors list.
     * @return the saved entity.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    public Film updateDirectors(Long filmId, @Nullable Collection<Person> directors) {
        Film film = getFilm(filmId);
        if (directors == null) {
            film.getDirectors().clear();
        } else {
            film.setDirectors(new HashSet<>(directors));
        }
        return saveFilm(film);
    }

    /**
     * Alias for the {@link FilmService#updateDirectors}({@code filmId, null}).
     * @param filmId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    public void deleteDirectors(Long filmId) {
        updateDirectors(filmId, null);
    }

    /**
     * Returns a collection of {@link Person} entities containing roles for a {@link Film} with the given
     * {@code filmId}.
     * @param filmId must not be {@code null}.
     * @return the roles.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    public Set<Role> getCast(Long filmId) {
        return getFilm(filmId).getRoles();
    }

    /**
     * Deletes all roles for a {@link Film} with the given {@code filmId}.
     * @param filmId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    public void deleteCast(Long filmId) {
        assertFilmExists(filmId);
        roleRepository.deleteById_FilmId(filmId);
    }

    /**
     * Throws 404 if a {@link Film} entity with the given {@code filmId} doesn't exist.
     * @param filmId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a film with the given {@code filmId} doesn't
     * exist.
     */
    private void assertFilmExists(Long filmId) {
        if (!filmRepository.existsById(filmId)) {
            throw getNotFoundException(filmId);
        }
    }

    private ResponseStatusException getNotFoundException(long id) {
        return getNotFoundException(List.of(id));
    }

    /**
     * Returns a {@link HttpStatus#NOT_FOUND} exception with a reason containing given {@code ids}.
     * @param ids to include in the reason.
     * @return {@link ResponseStatusException} with the response status and the reason.
     */
    private ResponseStatusException getNotFoundException(List<Long> ids) {
        Assert.notNull(ids, "Must provide id(s) for the '404 Not Found' exception.");
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find film(s) with id(s) " + ids);
    }
}
