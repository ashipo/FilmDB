package com.demo.filmdb.film;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

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

    @Nested
    @DisplayName("filmExists")
    class FilmExists {

        @Test
        @DisplayName("Existing film, returns true")
        void ExistingFilm_ReturnsTrue() {
            final Long filmId = 1L;
            given(filmRepository.existsById(filmId)).willReturn(true);

            boolean actual = filmService.filmExists(filmId);

            assertThat(actual).isTrue();
        }

        @Test
        @DisplayName("Not existing role, returns false")
        void NotExistingRole_ReturnsFalse() {
            final Long filmId = 1L;
            given(filmRepository.existsById(filmId)).willReturn(false);

            boolean actual = filmService.filmExists(filmId);

            assertThat(actual).isFalse();
        }
    }
}
