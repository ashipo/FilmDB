package com.demo.filmdb.film;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FilmService")
class FilmServiceTests extends ServiceTest {

    private FilmService filmService;
    private final FilmMapper filmMapper = Mappers.getMapper(FilmMapper.class);

    @BeforeEach
    void setUp() {
        filmService = new FilmService(filmRepository, roleRepository, filmMapper);
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

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.film.FilmServiceTests#updateFilmProvider")
        @DisplayName("Existing id, saves")
        void ExistingId_Updates(String expectedTitle, LocalDate expectedReleaseDate, String expectedSynopsis) {
            final Long filmId = 1L;
            final Film existingFilm = createFilm(filmId, "Tenet", LocalDate.of(2020, 8, 26), "Armed with only the word \"Tenet\"");
            // find existing person
            given(filmRepository.findById(filmId)).willReturn(Optional.of(existingFilm));
            // return updated person
            when(filmRepository.save(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

            Film actual = filmService.updateFilm(filmId, createFilmInfo(expectedTitle, expectedReleaseDate, expectedSynopsis));

            // assert saved
            var updatedFilmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(updatedFilmCaptor.capture());
            Film updatedFilm = updatedFilmCaptor.getValue();
            assertThat(updatedFilm.getId()).isEqualTo(filmId);
            assertThat(updatedFilm.getTitle()).isEqualTo(expectedTitle);
            assertThat(updatedFilm.getReleaseDate()).isEqualTo(expectedReleaseDate);
            assertThat(updatedFilm.getSynopsis()).isEqualTo(expectedSynopsis);
            // assert returned
            assertThat(actual.getId()).isEqualTo(filmId);
            assertThat(actual.getTitle()).isEqualTo(expectedTitle);
            assertThat(actual.getReleaseDate()).isEqualTo(expectedReleaseDate);
            assertThat(actual.getSynopsis()).isEqualTo(expectedSynopsis);
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        void NotExistingId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.updateFilm(1L, createFilmInfo("Interstellar", LocalDate.of(2014, 10, 26), "When Earth becomes uninhabitable"))
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
        @DisplayName("Existing film id, returns true")
        void ExistingFilm_ReturnsTrue() {
            final Long filmId = 1L;
            given(filmRepository.existsById(filmId)).willReturn(true);

            boolean actual = filmService.filmExists(filmId);

            assertThat(actual).isTrue();
        }

        @Test
        @DisplayName("Not existing film id, returns false")
        void NotExistingId_ReturnsFalse() {
            final Long filmId = 1L;
            given(filmRepository.existsById(filmId)).willReturn(false);

            boolean actual = filmService.filmExists(filmId);

            assertThat(actual).isFalse();
        }
    }

    // Util

    private static Stream<Arguments> updateFilmProvider() {
        final String title = "Inception";
        final LocalDate releaseDate = LocalDate.of(2010, 7, 8);
        final String synopsis = "A thief who steals";
        return Stream.of(
                Arguments.arguments(title, releaseDate, synopsis),
                Arguments.arguments(title, releaseDate, null)
        );
    }

    private FilmInfo createFilmInfo(String title, LocalDate releaseDate, @Nullable String synopsis) {
        return new FilmInfo() {
            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public LocalDate getReleaseDate() {
                return releaseDate;
            }

            @Override
            public String getSynopsis() {
                return synopsis;
            }
        };
    }
}
