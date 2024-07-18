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

    protected Film createFilm(long id) {
        Film result = new Film();
        result.setId(id);
        return result;
    }

    protected Person createPerson(long id) {
        Person result = new Person();
        result.setId(id);
        return result;
    }
}
