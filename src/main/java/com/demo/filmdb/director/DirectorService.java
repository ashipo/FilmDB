package com.demo.filmdb.director;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;
import static com.demo.filmdb.util.ErrorUtil.personNotFoundMessage;

@Service
public class DirectorService {

    private final FilmRepository filmRepository;
    private final PersonRepository personRepository;

    @Autowired
    public DirectorService(FilmRepository filmRepository, PersonRepository personRepository) {
        this.filmRepository = filmRepository;
        this.personRepository = personRepository;
    }

    /**
     * Sets a {@linkplain  Person} as a director for a {@linkplain Film}
     *
     * @param filmId    directed film id
     * @param personId  director id
     * @throws EntityNotFoundException if film or person could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void setDirector(Long filmId, Long personId) throws EntityNotFoundException {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new EntityNotFoundException(filmNotFoundMessage(filmId)));
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException(personNotFoundMessage(personId)));
        film.addDirector(person);
        filmRepository.save(film);
    }

    /**
     * Delete a {@linkplain Person} from directors of a {@linkplain Film}
     *
     * @param filmId    directed film id
     * @param personId  director id
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDirector(Long filmId, Long personId) {
        Optional<Film> filmOptional = filmRepository.findById(filmId);
        if (filmOptional.isEmpty()) {
            return;
        }
        Optional<Person> personOptional = personRepository.findById(personId);
        if (personOptional.isEmpty()) {
            return;
        }
        Film film = filmOptional.get();
        Person director = personOptional.get();
        film.removeDirector(director);
        filmRepository.save(film);
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
        Film film = filmRepository.findById(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        if (directorsIds == null || directorsIds.isEmpty()) {
            film.getDirectors().clear();
            return filmRepository.save(film);
        }

        List<Person> directors = personRepository.findAllById(directorsIds);
        if (directors.size() != directorsIds.size()) {
            List<Long> notFoundIds = notFoundIds(directorsIds, directors);
            throw new EntityNotFoundException("Could not find people with ids " + notFoundIds);
        }
        film.setDirectors(directors);
        return filmRepository.save(film);
    }

    /**
     * Removes all directors for the {@link Film} with the given {@code filmId}.
     * Same as {@link DirectorService#updateDirectors}({@code filmId, null}).
     *
     * @param filmId id of the film
     * @throws EntityNotFoundException if film could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDirectors(Long filmId) throws EntityNotFoundException {
        updateDirectors(filmId, null);
    }

    private List<Long> notFoundIds(Collection<Long> requestedIds, Collection<Person> foundPeople) {
        Set<Long> existingIds = foundPeople.stream().map(Person::getId).collect(Collectors.toSet());
        return requestedIds.stream().filter(Predicate.not(existingIds::contains)).toList();
    }
}
