package com.demo.filmdb.film;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.utils.SortUtil;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;

@Service
public class FilmService {

    private final FilmRepository filmRepository;
    private final RoleRepository roleRepository;
    private final FilmInfoMapper filmMapper;
    private final FilmSpecs filmSpecs;

    @Autowired
    public FilmService(FilmRepository filmRepository, RoleRepository roleRepository, FilmInfoMapper filmMapper, FilmSpecs filmSpecs) {
        this.filmRepository = filmRepository;
        this.roleRepository = roleRepository;
        this.filmMapper = filmMapper;
        this.filmSpecs = filmSpecs;
    }

    /**
     * Returns a {@link Page} of {@link Film} entities
     *
     * @param pageable  must not be null
     * @return the resulting page, may be empty but not null
     */
    public Page<Film> getFilms(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Film.class);
        return filmRepository.findAll(filteredPageable);
    }

    /**
     * Returns a {@link Page} of {@link Film} entities.
     * For filtering any of {@code title}, {@code releaseAfter} or {@code releaseBefore} can be specified.
     *
     * @param pageable      must not be null
     * @param title         string that films must contain in their title. Should not be blank. Can be null.
     * @param releaseAfter  release date lower limit. Can be null.
     * @param releaseBefore release date upper limit. Can be null.
     * @return the resulting page, may be empty but not null
     */
    public Page<Film> getFilms(
            Pageable pageable,
            @Nullable String title,
            @Nullable LocalDate releaseAfter,
            @Nullable LocalDate releaseBefore
    ) {
        Specification<Film> spec = filmSpecs.titleContains(title)
                .and(filmSpecs.releaseBefore(releaseBefore))
                .and(filmSpecs.releaseAfter(releaseAfter));
        Pageable filteredPageable = SortUtil.filterSort(pageable, Film.class);
        return filmRepository.findAll(spec, filteredPageable);
    }

    /**
     * Returns a page of {@link Film} entities.
     * For sorting both {@code sortBy} and {@code sortDirection} must not be null.
     * For filtering any of {@code title}, {@code releaseAfter} or {@code releaseBefore} can be specified.
     *
     * @param page          page number
     * @param pageSize      page size
     * @param sortBy        {@link Film} property name to sort by. Can be null.
     * @param sortDirection sort direction. Can be null.
     * @param title         string that films must contain in their title. Should not be blank. Can be null.
     * @param releaseAfter  release date lower limit. Can be null.
     * @param releaseBefore release date upper limit. Can be null.
     * @return the resulting page, may be empty but not null
     */
    public Page<Film> getFilms(
            int page,
            int pageSize,
            @Nullable String sortBy,
            @Nullable Sort.Direction sortDirection,
            @Nullable String title,
            @Nullable LocalDate releaseAfter,
            @Nullable LocalDate releaseBefore
    ) {
        Pageable pageable;
        if (sortBy == null || sortDirection == null) {
            pageable = PageRequest.of(page, pageSize);
        } else {
            pageable = PageRequest.of(page, pageSize, sortDirection, sortBy);
        }
        return getFilms(pageable, title, releaseAfter, releaseBefore);
    }

    /**
     * Create a {@link Film}
     *
     * @param filmInfo film info
     * @return the created entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Film createFilm(FilmInfo filmInfo) {
        final Film film = filmMapper.filmInfoToFilm(filmInfo);
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

    /**
     * Returns film cast
     *
     * @param filmId must not be null
     * @return collection of roles
     * @throws EntityNotFoundException if film could not be found
     */
    public Collection<Role> getCast(Long filmId) throws EntityNotFoundException {
        Film film = filmRepository.findById(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        return film.getCast();
    }

    /**
     * Returns film directors
     *
     * @param filmId must not be null
     * @return collection of people
     * @throws EntityNotFoundException if film could not be found
     */
    public Collection<Person> getDirectors(Long filmId) throws EntityNotFoundException {
        Film film = filmRepository.findById(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        return film.getDirectors();
    }
}
