package com.demo.filmdb.film;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.role.RoleRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;

@Service
public class FilmService {

    private PersonService personService;

    private final FilmRepository filmRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository, RoleRepository roleRepository) {
        this.filmRepository = filmRepository;
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setPersonService(@Lazy PersonService personService) {
        this.personService = personService;
    }

    /**
     * Returns a {@link Page} of {@link Film} entities matching the given {@link Specification}.
     *
     * @param spec must not be {@code null}.
     * @param pageable must not be {@code null}.
     * @return a page of filtered entities.
     */
    public Page<Film> search(Specification<Film> spec, Pageable pageable) {
        return filmRepository.findAll(spec, pageable);
    }

    /**
     * Returns a {@link Page} of all {@link Film} entities.
     *
     * @param pageable must not be {@code null}.
     * @return a page.
     */
    public Page<Film> getAllFilms(Pageable pageable) {
        return filmRepository.findAll(pageable);
    }

    /**
     * Saves the given {@link Film} entity.
     *
     * @param film must not be {@code null}.
     * @return the saved entity.
     */
    public Film saveFilm(Film film) {
        return filmRepository.save(film);
    }

    /**
     * Returns a {@link Film} entity with the given id or null if it doesn't exist.
     *
     * @param filmId must not be {@code null}.
     * @return the found entity.
     */
    public @Nullable Film getFilm(Long filmId) {
        return filmRepository.findById(filmId).orElse(null);
    }

    /**
     * Deletes the given {@link Film} entity.
     *
     * @param film to delete.
     */
    @Transactional
    public void deleteFilm(Film film) {
        roleRepository.deleteById_FilmId(film.getId());
        filmRepository.delete(film);
    }

    /**
     * Deletes a {@link Film} entity with the given id.
     *
     * @param filmId id.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFilmById(Long filmId) {
        roleRepository.deleteById_FilmId(filmId);
        filmRepository.deleteById(filmId);
    }

    /**
     * Replaces directors for the {@link Film} with the given {@code filmId}
     *
     * @param filmId id of the film
     * @param directorsIds ids of the {@link Person} entities to set as directors for the film.
     *                     Set to {@code null} to remove all directors.
     * @return the updated entity
     * @throws EntityNotFoundException if film or any of the directors could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Film updateDirectors(Long filmId, @Nullable Collection<Long> directorsIds) throws EntityNotFoundException {
        Film film = getFilm(filmId);
        if (film == null) {
            throw new EntityNotFoundException(filmNotFoundMessage(filmId));
        }
        if (directorsIds == null || directorsIds.isEmpty()) {
            film.getDirectors().clear();
            return saveFilm(film);
        }

        List<Person> directors = personService.getPeople(directorsIds);
        if (directors.size() != directorsIds.size()) {
            List<Long> notFoundIds = notFoundIds(directorsIds, directors);
            throw new EntityNotFoundException("Could not find people with ids " + notFoundIds);
        }
        film.setDirectors(new HashSet<>(directors));
        return saveFilm(film);
    }

    /**
     * Removes all directors for the {@link Film} with the given {@code filmId}.
     * Same as {@link FilmService#updateDirectors}({@code filmId, null}).
     *
     * @param filmId id of the film
     * @throws EntityNotFoundException if film could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDirectors(Long filmId) throws EntityNotFoundException {
        updateDirectors(filmId, null);
    }

    /**
     * Deletes all roles for a {@link Film} with the given {@code filmId}.
     *
     * @param filmId must not be {@code null}.
     */
    public void deleteCast(Long filmId) {
        roleRepository.deleteById_FilmId(filmId);
    }

    private List<Long> notFoundIds(Collection<Long> requestedIds, Collection<Person> foundPeople) {
        Set<Long> existingIds = foundPeople.stream().map(Person::getId).collect(Collectors.toSet());
        return requestedIds.stream().filter(Predicate.not(existingIds::contains)).toList();
    }
}
