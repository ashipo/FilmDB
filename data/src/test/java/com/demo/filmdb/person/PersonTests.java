package com.demo.filmdb.person;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.role.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.demo.filmdb.util.Creators.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;

@DisplayName("Person")
public class PersonTests {

    @Nested
    class addFilmDirected {

        @Test
        @DisplayName("Should only modify the directed films of a Person")
        void doesNotModifyFilm() {
            Person person = createPerson(2L);
            Film film = createFilm(1L);

            person.addFilmDirected(film);

            assertThatCollection(film.getDirectors()).doesNotContain(person);
            assertThatCollection(person.getFilmsDirected()).contains(film);

        }
    }

    @Nested
    class getFilmsDirected {

        @Test
        @DisplayName("The returned collection should be immutable")
        void returnsImmutableCollection() {
            Person person = createPerson(2L);

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                    person.getFilmsDirected().add(createFilm(2L))
            );
        }
    }

    @Nested
    class removeFilmDirected {

        @Test
        @DisplayName("Should only modify the directed films of a Person")
        void doesNotModifyFilm() {
            Person person = createPerson(2L);
            Film film = createFilm(1L);
            film.addDirector(person);

            person.removeFilmDirected(film);

            assertThatCollection(film.getDirectors()).contains(person);
            assertThatCollection(person.getFilmsDirected()).doesNotContain(film);

        }
    }

    @Nested
    class removeFilmsDirected {

        @Test
        @DisplayName("Removes correctly from both sides")
        void removesCorrectly() {
            Film film = createFilm(1L);
            Person person = createPerson(2L);
            film.addDirector(person);

            person.removeFilmsDirected();

            assertThatCollection(film.getDirectors()).doesNotContain(person);
            assertThatCollection(person.getFilmsDirected()).doesNotContain(film);
        }
    }

    @Nested
    class getRoles {

        @Test
        @DisplayName("The returned collection should be immutable")
        void returnsImmutableCollection() {
            Person person = createPerson(2L);

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                    person.getRoles().add(createRole())
            );
        }
    }

    @Nested
    class addRole {

        @Test
        @DisplayName("Given null Role, throws NPE")
        void giveNullRole_throws() {
            Person entity = createPerson(1L);

            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
                    entity.addRole(null)
            );
        }

        @Test
        @DisplayName("Given Role that is played by another Person, throws IllegalArgumentException")
        void givenOtherPersonRole_throws() {
            Person person = new Person("Roy Scheider", LocalDate.of(1932, 11, 10));
            Person anotherPerson = new Person("Robert Shaw", LocalDate.of(1927, 8, 9));
            Film film = createFilm(10L);
            Role role = new Role(film, anotherPerson, "Quint");

            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                    person.addRole(role)
            );
        }
    }
}
