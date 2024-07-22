package com.demo.filmdb;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.role.RoleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private Film createFilmWithoutId() {
        return new Film("Shining", LocalDate.of(1980, 5, 23), "A family heads to an isolated hotel.");
    }

    protected Film createFilm(long id) {
        Film result = createFilmWithoutId();
        result.setId(id);
        return result;
    }

    protected Film createFilm() {
        Film result = createFilmWithoutId();
        result.setId(42L);
        return result;
    }

    protected Person createPerson(long id) {
        Person result = new Person();
        result.setId(id);
        return result;
    }
}
