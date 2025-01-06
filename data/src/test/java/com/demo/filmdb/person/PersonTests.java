package com.demo.filmdb.person;

import com.demo.filmdb.film.Film;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.demo.filmdb.util.Creators.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;

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
}
