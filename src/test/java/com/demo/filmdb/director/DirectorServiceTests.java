package com.demo.filmdb.director;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("DirectorService")
public class DirectorServiceTests extends ServiceTest {

    private DirectorService directorService;

    @BeforeEach
    void setUp() {
        directorService = new DirectorService(filmRepository, personRepository);
    }

    @Nested
    @DisplayName("setDirector")
    class SetDirector {

        @Test
        @DisplayName("Existing ids, sets")
        void ExistingIds_Sets() {
            Long filmId = 2L;
            Long personId = 3L;
            Person expectedPerson = createPerson(personId);
            given(filmRepository.findById(filmId)).willReturn(Optional.of(createFilm(filmId)));
            given(personRepository.findById(personId)).willReturn(Optional.of(expectedPerson));

            directorService.setDirector(filmId, personId);

            ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).contains(expectedPerson);
        }

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        void NotExistingFilmId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.setDirector(1L, 2L)
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.setDirector(1L, 2L)
            );
        }
    }

    @Nested
    @DisplayName("deleteDirector")
    class DeleteDirector {

        @Test
        @DisplayName("Deletes correctly")
        void ExistingIds_DeletesCorrectly() {
            final Long filmId = 1L;
            final Long personId = 2L;
            Film film = createFilm(filmId);
            Person director = createPerson(personId);
            Set<Person> directors = new HashSet<>();
            directors.add(director);
            film.setDirectors(directors);
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));
            given(personRepository.findById(personId)).willReturn(Optional.of(director));

            directorService.deleteDirector(filmId, personId);

            ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateDirectors")
    class UpdateDirectors {

        @Captor
        ArgumentCaptor<Film> filmCaptor;

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        public void NotExistingFilmId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.updateDirectors(1L, null)
            );
        }

        @Test
        @DisplayName("Not existing people ids, throws EntityNotFoundException")
        public void NotExistingPeopleId_Throws() {
            List<Long> directorsIds = List.of(1L, 2L);
            List<Person> directors = List.of(createPerson(1L));
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));
            given(personRepository.findAllById(any())).willReturn(directors);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.updateDirectors(1L, directorsIds)
            );
        }

        @Test
        @DisplayName("Existing ids, saves correctly")
        public void ExistingIds_SavesCorrectly() {
            Long filmId = 5L;
            List<Long> directorsIds = List.of(1L, 2L);
            List<Person> directors = new ArrayList<>();
            for (Long id : directorsIds) {
                directors.add(createPerson(id));
            }
            given(filmRepository.findById(filmId)).willReturn(Optional.of(createFilm(filmId)));
            given(personRepository.findAllById(any())).willReturn(directors);

            directorService.updateDirectors(filmId, directorsIds);

            verify(filmRepository).save(filmCaptor.capture());
            assertThat(filmCaptor.getValue().getId()).isEqualTo(filmId);
            assertThatCollection(filmCaptor.getValue().getDirectors()).containsExactlyElementsOf(directors);
        }

        @Test
        @DisplayName("Null directors, saves empty directors")
        public void NullDirectors_SavesCorrectly() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            directorService.updateDirectors(1L, null);

            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteDirectors")
    class DeleteDirectors {

        @Test
        @DisplayName("Existing id, saves empty directors")
        public void ExistingId_SavesEmptyDirectors(@Captor ArgumentCaptor<Film> filmCaptor) {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            directorService.deleteDirectors(1L);

            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).isEmpty();
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        public void NotExistingId_Throws() {
            given(filmRepository.findById(anyLong())).willThrow(EntityNotFoundException.class);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.deleteDirectors(1L)
            );
        }
    }
}
