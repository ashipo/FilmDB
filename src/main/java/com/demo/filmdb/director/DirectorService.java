package com.demo.filmdb.director;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * Delete a {@linkplain  Person} from directors of a {@linkplain Film}
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
}
