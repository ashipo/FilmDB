package com.demo.filmdb.film;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FilmService")
class FilmServiceTests extends ServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(filmRepository, roleRepository);
        filmService.setPersonService(personService);
    }

    @Test
    @DisplayName("search - finds")
    void search_ValidArguments_Finds() {
        Specification<Film> expectedSpec = Specification.where(new FilmWithTitle("title"));
        Pageable expectedPageable = PageRequest.of(1, 5);

        filmService.search(expectedSpec, expectedPageable);

        verify(filmRepository).findAll(expectedSpec, expectedPageable);
    }

    @Test
    @DisplayName("getAllFilms - finds")
    void getAllFilms_Finds() {
        final Pageable expectedPageable = Pageable.unpaged();

        filmService.getAllFilms(expectedPageable);

        verify(filmRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("saveFilm - saves and returns the saved Entity")
    void saveFilm_ValidFilm_SavesFilm() {
        Film expected = createFilm();
        when(filmRepository.save(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

        Film actual = filmService.saveFilm(expected);

        verify(filmRepository).save(expected);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Nested
    @DisplayName("getFilm")
    class GetFilm {

        @Test
        @DisplayName("Existing id, finds and returns the found Entity")
        void ExistingId_ReturnsFilm() {
            final Long expectedFilmId = 9L;
            given(filmRepository.findById(expectedFilmId)).willReturn(Optional.of(createFilm(expectedFilmId)));

            var actual = filmService.getFilm(expectedFilmId);

            verify(filmRepository).findById(expectedFilmId);
            assert actual.isPresent();
            assertThat(actual.get().getId()).isEqualTo(expectedFilmId);
        }

        @Test
        @DisplayName("Not existing id, returns null")
        void NotExistingId_ReturnsNull() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            var actual = filmService.getFilm(9L);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateFilm")
    class UpdateFilm {

        @Test
        @DisplayName("Existing id, saves")
        void ExistingId_Saves() {
            given(filmRepository.existsById(anyLong())).willReturn(true);
            when(filmRepository.save(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());
            Film expected = createFilm();

            Film actual = filmService.updateFilm(expected);

            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        void NotExistingId_Saves() {
            given(filmRepository.existsById(anyLong())).willReturn(false);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.updateFilm(createFilm())
            );
        }
    }

    @Nested
    @DisplayName("deleteFilm")
    class DeleteFilm {

        @Test
        @DisplayName("Deletes correctly")
        void ExistingId_DeletesFilm() {
            final Long expectedId = 1L;

            filmService.deleteFilm(expectedId);

            verify(filmRepository).deleteById(expectedId);
            verify(roleRepository).deleteById_FilmId(expectedId);
        }
    }

    /* Directors */

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
                    filmService.updateDirectors(1L, null)
            );
        }

        @Test
        @DisplayName("Not existing people ids, throws EntityNotFoundException")
        public void NotExistingPeopleId_Throws() {
            List<Long> directorsIds = List.of(1L, 2L);
            List<Person> directors = List.of(createPerson(1L));
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));
            given(personService.getPeople(any())).willReturn(directors);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.updateDirectors(1L, directorsIds));
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
            given(personService.getPeople(any())).willReturn(directors);

            filmService.updateDirectors(filmId, directorsIds);

            verify(filmRepository).save(filmCaptor.capture());
            assertThat(filmCaptor.getValue().getId()).isEqualTo(filmId);
            assertThatCollection(filmCaptor.getValue().getDirectors()).containsExactlyElementsOf(directors);
        }

        @Test
        @DisplayName("Null directors, saves empty directors")
        public void NullDirectors_SavesCorrectly() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.updateDirectors(1L, null);

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
            given(filmRepository.findById(anyLong()))
                    .willReturn(Optional.of(new Film()));

            filmService.deleteDirectors(1L);

            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).isEmpty();
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        public void NotExistingId_Throws() {
            given(filmRepository.findById(anyLong()))
                    .willThrow(EntityNotFoundException.class);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.deleteDirectors(1L));
        }
    }

    /* Cast */

    @Nested
    class DeleteCast {
        @Test
        public void ExistingId_Deletes() {
            final Long expectedFilmId = 1L;

            filmService.deleteCast(expectedFilmId);

            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }
    }
}
