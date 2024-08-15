package com.demo.filmdb.film;

import com.demo.filmdb.role.RoleRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;

@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final RoleRepository roleRepository;
    private final FilmMapper filmMapper;

    @Autowired
    public FilmService(FilmRepository filmRepository, RoleRepository roleRepository, FilmMapper filmMapper) {
        this.filmRepository = filmRepository;
        this.roleRepository = roleRepository;
        this.filmMapper = filmMapper;
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
    @PreAuthorize("hasRole('ADMIN')")
    public Film saveFilm(Film film) {
        return filmRepository.save(film);
    }

    /**
     * Returns a {@linkplain Film} entity with the given id or empty {@code Optional} if it doesn't exist
     *
     * @param filmId must not be {@code null}
     * @return the found entity or empty {@code Optional}
     */
    public Optional<Film> getFilm(Long filmId) {
        return filmRepository.findById(filmId);
    }

    /**
     * Saves the given {@link Film} entity if its {@code id} already exists
     *
     * @param film entity to update
     * @return updated entity
     * @throws EntityNotFoundException if the given {@code film} contains {@code id} that doesn't exist
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Film updateFilm(Film film) throws EntityNotFoundException {
        if (!filmRepository.existsById(film.getId())) {
            throw new EntityNotFoundException(filmNotFoundMessage(film.getId()));
        }
        return filmRepository.save(film);
    }

    /**
     * Update a {@link Film}
     *
     * @param filmId film to update
     * @param filmInfo film info
     * @return the updated entity
     * @throws EntityNotFoundException if film could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Film updateFilm(Long filmId, FilmInfo filmInfo) throws EntityNotFoundException {
        Film filmToUpdate = filmRepository.findById(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        filmMapper.updateFilmFromFilmInfo(filmInfo, filmToUpdate);
        return filmRepository.save(filmToUpdate);
    }

    /**
     * Deletes a {@link Film} entity with the given id.
     *
     * @param filmId id.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFilm(Long filmId) {
        roleRepository.deleteById_FilmId(filmId);
        filmRepository.deleteById(filmId);
    }

    /**
     * Returns whether a {@link Film} with the given id exists
     *
     * @param filmId must not be {@code null}
     * @return true if exists, false otherwise
     */
    public boolean filmExists(Long filmId) {
        return filmRepository.existsById(filmId);
    }
}
