package com.demo.filmdb;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.role.RoleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceTest {

    @Mock
    protected FilmRepository filmRepository;
    @Mock
    protected PersonRepository personRepository;
    @Mock
    protected RoleRepository roleRepository;

    protected void assertThatValid404(Throwable thrown){
        assertThat(thrown).as("Expected ResponseStatusException to be thrown")
                .isInstanceOf(ResponseStatusException.class);
        ResponseStatusException statusException = (ResponseStatusException) thrown;
        assertThat(statusException.getStatusCode().value()).as("Status code").isEqualTo(404);
    }

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
