package com.demo.filmdb.film;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;

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
     * Replaces directors for the given {@link Film} entity.
     *
     * @param film to replace directors in.
     * @param directors a collection of {@link Person} entities to set as directors for the {@code film}.
     *                  Set to {@code null} to remove all directors.
     * @return the saved entity.
     */
    public Film updateDirectors(Film film, @Nullable Collection<Person> directors) {
        if (directors == null) {
            film.getDirectors().clear();
        } else {
            film.setDirectors(new HashSet<>(directors));
        }
        return saveFilm(film);
    }

    /**
     * Alias for the {@link FilmService#updateDirectors}({@code film, null}).
     *
     * @param film to delete.
     */
    public void deleteDirectors(Film film) {
        updateDirectors(film, null);
    }

    /**
     * Deletes all roles for a {@link Film} with the given {@code filmId}.
     *
     * @param filmId must not be {@code null}.
     */
    public void deleteCast(Long filmId) {
        roleRepository.deleteById_FilmId(filmId);
    }
}
