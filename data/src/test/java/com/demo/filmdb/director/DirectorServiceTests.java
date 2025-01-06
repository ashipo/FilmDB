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

import java.util.List;
import java.util.Optional;

import static com.demo.filmdb.util.Creators.createFilm;
import static com.demo.filmdb.util.Creators.createPerson;
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
            Film capturedFilm = filmCaptor.getValue();
            // Assert that director was added to the film's directors collection
            assertThatCollection(capturedFilm.getDirectors()).contains(expectedPerson);
            // Assert that film was added to director's directed films collection
            assertThatCollection(expectedPerson.getFilmsDirected()).contains(capturedFilm);
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
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(createFilm(1L)));
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
            film.setDirectors(List.of(director));
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));
            given(personRepository.findById(personId)).willReturn(Optional.of(director));

            directorService.deleteDirector(filmId, personId);

            ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmCaptor.capture());
            Film filmCaptured = filmCaptor.getValue();
            assertThatCollection(filmCaptured.getDirectors()).doesNotContain(director);
            assertThatCollection(director.getFilmsDirected()).doesNotContain(filmCaptured);
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
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(createFilm(1L)));
            given(personRepository.findAllById(any())).willReturn(directors);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.updateDirectors(1L, directorsIds)
            );
        }

        @Test
        @DisplayName("Existing ids, saves correctly")
        public void ExistingIds_SavesCorrectly() {
            // Directors setup
            Person removedDirector = createPerson(1L);
            Person keptDirector = createPerson(2L);
            Person newDirector = createPerson(3L);
            List<Person> oldDirectors = List.of(removedDirector, keptDirector);
            List<Person> newDirectors = List.of(keptDirector, newDirector);
            List<Long> newDirectorsIds = List.of(2L, 3L);
            // Film setup
            Long filmId = 5L;
            Film film = createFilm(filmId);
            film.setDirectors(oldDirectors);
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));
            given(personRepository.findAllById(any())).willReturn(newDirectors);
            for (Person director : oldDirectors) {
                assertThatCollection(director.getFilmsDirected()).contains(film);
            }

            directorService.updateDirectors(filmId, newDirectorsIds);

            verify(filmRepository).save(filmCaptor.capture());
            Film capturedFilm = filmCaptor.getValue();
            assertThat(capturedFilm.getId()).isEqualTo(filmId);
            // Assert that only the new directors are in the film's directors collection
            assertThatCollection(capturedFilm.getDirectors()).containsExactlyElementsOf(newDirectors);
            // Assert that the film is in each of the new director's directed films
            for (Person director : newDirectors) {
                assertThatCollection(director.getFilmsDirected()).contains(capturedFilm);
            }
            // Assert that the film is not in the removed director's directed films
            assertThatCollection(removedDirector.getFilmsDirected()).doesNotContain(capturedFilm);
        }

        @Test
        @DisplayName("Null directors, saves empty directors")
        public void NullDirectors_SavesCorrectly() {
            // Film with a director
            Person director = createPerson(1L);
            Long filmId = 5L;
            Film film = createFilm(filmId);
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));
            film.addDirector(director);
            assertThatCollection(director.getFilmsDirected()).contains(film);

            directorService.updateDirectors(filmId, null);

            verify(filmRepository).save(filmCaptor.capture());
            Film capturedFilm = filmCaptor.getValue();
            assertThatCollection(capturedFilm.getDirectors()).doesNotContain(director);
            assertThatCollection(director.getFilmsDirected()).doesNotContain(film);
        }
    }

    @Nested
    @DisplayName("deleteDirectors")
    class DeleteDirectors {

        @Test
        @DisplayName("Existing id, saves empty directors")
        public void ExistingId_SavesEmptyDirectors(@Captor ArgumentCaptor<Film> filmCaptor) {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(createFilm(1L)));

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
