package com.demo.filmdb;

import com.demo.filmdb.film.FilmRepository;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.person.PersonRepository;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.role.RoleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

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

    protected <T> Specification<T> emptySpec() {
        return Specification.where((r, q, c) -> null);
    }
}
