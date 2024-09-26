package com.demo.filmdb;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceTest {

    @Mock
    protected FilmRepository filmRepository;
    @Mock
    protected PersonRepository personRepository;
    @Mock
    protected RoleRepository roleRepository;
    @Mock
    protected PersonService personService;
    @Mock
    protected FilmService filmService;

    protected Film createFilm(Long id, String title, LocalDate releaseDate, String synopsis) {
        Film result = new Film(title, releaseDate, synopsis);
        result.setId(id);
        return result;
    }

    protected Film createFilm(Long id) {
        return createFilm(id, "Shining", LocalDate.of(1980, 5, 23), "A family heads to an isolated hotel.");
    }

    protected Person createPerson(Long id) {
        return createPerson(id, "John Doe " + id, LocalDate.of(2000, 2, 29));
    }

    protected Person createPerson(Long id, String name, LocalDate dateOfBirth) {
        Person result = new Person(name, dateOfBirth);
        result.setId(id);
        return result;
    }

    protected Role createRole(Long filmId, Long personId, String character) {
        return new Role(createFilm(filmId), createPerson(personId), character);
    }

    protected <T> Specification<T> emptySpec() {
        return Specification.where((r, q, c) -> null);
    }
}
