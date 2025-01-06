package com.demo.filmdb.film;

import com.demo.filmdb.person.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.demo.filmdb.util.Creators.*;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FilmTests {

    @Nested
    class addDirector {

        @Test
        @DisplayName("Adds correctly to the both sides of the association")
        void addsCorrectly() {
            Person person = createPerson(1L);
            Film film = createFilm(2L);

            film.addDirector(person);

            assertThatCollection(film.getDirectors()).contains(person);
            assertThatCollection(person.getFilmsDirected()).contains(film);
        }
    }

    @Nested
    class getDirectors {

        @Test
        @DisplayName("The returned collection should be immutable")
        void returnsImmutableCollection() {
            Film film = createFilm(1L);

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                    film.getDirectors().add(createPerson(2L))
            );
        }
    }

    @Nested
    class setDirectors {

        @Test
        @DisplayName("Correctly removes from the both sides of the association")
        void removesCorrectly() {
            // Directors setup
            Person toBeRemovedDirector = createPerson(1L);
            Person newDirector = createPerson(3L);
            // Film setup
            Film film = createFilm(5L);
            film.addDirector(toBeRemovedDirector);

            film.setDirectors(List.of(newDirector));

            assertThatCollection(film.getDirectors()).doesNotContain(toBeRemovedDirector);
            assertThatCollection(toBeRemovedDirector.getFilmsDirected()).doesNotContain(film);
        }

        @Test
        @DisplayName("Correctly adds to the both sides of the association")
        void addsCorrectly() {
            Person person = createPerson(3L);
            Film film = createFilm(5L);

            film.setDirectors(List.of(person));

            assertThatCollection(film.getDirectors()).contains(person);
            assertThatCollection(person.getFilmsDirected()).contains(film);
        }

        @Test
        @DisplayName("Correctly keeps an existing association")
        void keepsCorrectly() {
            Person person = createPerson(3L);
            Film film = createFilm(5L);
            film.addDirector(person);

            film.setDirectors(List.of(person));

            assertThatCollection(film.getDirectors()).contains(person);
            assertThatCollection(person.getFilmsDirected()).contains(film);
        }
    }

    @Nested
    class removeDirector {

        @Test
        @DisplayName("Correctly removes from the both sides of the association")
        void removesCorrectly() {
            Person person = createPerson(1L);
            Film film = createFilm(5L);
            film.addDirector(person);

            film.removeDirector(person);

            assertThatCollection(film.getDirectors()).doesNotContain(person);
        }
    }

    @Nested
    class removeDirectors {

        @Test
        @DisplayName("Correctly removes from the both sides of the association")
        void removesCorrectly() {
            var directors = List.of(
                    createPerson(1L),
                    createPerson(2L)
            );
            Film film = createFilm(5L);
            directors.forEach(film::addDirector);

            film.removeDirectors();

            assertThatCollection(film.getDirectors()).isEmpty();
            directors.forEach(person ->
                    assertThatCollection(person.getFilmsDirected()).doesNotContain(film)
            );
        }
    }

    @Nested
    class getCast {

        @Test
        @DisplayName("The returned collection should be immutable")
        void returnsImmutableCollection() {
            Film film = createFilm(1L);

            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                    film.getCast().add(createRole())
            );
        }
    }
}
