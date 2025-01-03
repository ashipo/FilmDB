package com.demo.filmdb.util;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;

import java.time.LocalDate;

public abstract class Creators {

    public static Film createFilm(Long id) {
        return new Film(id, "Shining", LocalDate.of(1980, 5, 23), "A family heads to an isolated hotel.");
    }

    public static Person createPerson(Long id) {
        return createPerson(id, "John Doe " + id, LocalDate.of(2000, 2, 29));
    }

    public static Person createPerson(Long id, String name, LocalDate dateOfBirth) {
        Person result = new Person(name, dateOfBirth);
        result.setId(id);
        return result;
    }

    public static Role createRole(Long filmId, Long personId, String character) {
        return new Role(createFilm(filmId), createPerson(personId), character);
    }
}
