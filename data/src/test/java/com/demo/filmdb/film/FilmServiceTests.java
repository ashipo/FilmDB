package com.demo.filmdb.film;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.util.EntityNotFoundException;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("FilmService")
class FilmServiceTests extends ServiceTest {

    private FilmService filmService;
    private final FilmInfoMapper filmMapper = Mappers.getMapper(FilmInfoMapper.class);
    @Mock
    private FilmSpecs filmSpecs;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(filmRepository, roleRepository, filmMapper, filmSpecs);
    }

    @Nested
    @DisplayName("getFilms")
    class GetFilms {

        @Nested
        @DisplayName("with pageable parameter")
        class WithPageable {

            @Test
            @DisplayName("Valid arguments, searches correctly")
            void ValidArguments_SearchesCorrectly() {
                int pageNumber = 13;
                int pageSize = 37;
                var direction = Sort.Direction.DESC;
                String sortBy = "title";
                var pageable = PageRequest.of(pageNumber, pageSize, direction, sortBy);

                filmService.getFilms(pageable);

                var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(filmRepository).findAll(pageableCaptor.capture());
                var actualPageable = pageableCaptor.getValue();
                assertThat(actualPageable.getPageNumber()).isEqualTo(pageNumber);
                assertThat(actualPageable.getPageSize()).isEqualTo(pageSize);
                var actualSort = actualPageable.getSort().getOrderFor(sortBy);
                assertThat(actualSort).isNotNull();
                assert actualSort != null;
                assertThat(actualSort.getDirection()).isEqualTo(direction);
            }
        }

        @Nested
        @DisplayName("with pageable and filter parameters")
        class WithPageableAndFilter {

            @Test
            @DisplayName("Valid arguments, searches correctly")
            void ValidArguments_SearchesCorrectly() {
                int pageNumber = 13;
                int pageSize = 37;
                var direction = Sort.Direction.DESC;
                String sortBy = "title";
                var pageable = PageRequest.of(pageNumber, pageSize, direction, sortBy);
                String title = "world";
                LocalDate releaseBefore = LocalDate.of(2222, 2, 2);
                given(filmSpecs.titleContains(title)).willReturn(emptySpec());
                given(filmSpecs.releaseAfter(null)).willReturn(emptySpec());
                given(filmSpecs.releaseBefore(releaseBefore)).willReturn(emptySpec());

                filmService.getFilms(pageable, title, null, releaseBefore);

                var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(filmRepository).findAll(ArgumentMatchers.<Specification<Film>>any(), pageableCaptor.capture());
                // verify paging
                var actualPageable = pageableCaptor.getValue();
                assertThat(actualPageable.getPageNumber()).isEqualTo(pageNumber);
                assertThat(actualPageable.getPageSize()).isEqualTo(pageSize);
                // verify sorting
                var actualSort = actualPageable.getSort().getOrderFor(sortBy);
                assertThat(actualSort).isNotNull();
                assert actualSort != null;
                assertThat(actualSort.getDirection()).isEqualTo(direction);
                // verify filtering
                verify(filmSpecs).titleContains(title);
                verify(filmSpecs).releaseAfter(null);
                verify(filmSpecs).releaseBefore(releaseBefore);
            }
        }

        @Nested
        @DisplayName("with separate parameters for paging, sorting and filtering")
        class WithSeparateParams {

            @Captor
            ArgumentCaptor<Pageable> pageableCaptor;

            @Test
            @DisplayName("Valid paging, calls repository correctly")
            void PagingArguments_CallsRepositoryCorrectly() {
                int pageNumber = 4;
                int pageSize = 8;
                given(filmSpecs.titleContains(null)).willReturn(emptySpec());
                given(filmSpecs.releaseAfter(null)).willReturn(emptySpec());
                given(filmSpecs.releaseBefore(null)).willReturn(emptySpec());

                filmService.getFilms(pageNumber, pageSize, null, null, null, null, null);

                verify(filmRepository).findAll(ArgumentMatchers.<Specification<Film>>any(), pageableCaptor.capture());
                Pageable pageable = pageableCaptor.getValue();
                assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
                assertThat(pageable.getPageSize()).isEqualTo(pageSize);
            }

            @Test
            @DisplayName("Valid sorting, calls repository correctly")
            void SortArguments_CallsRepositoryCorrectly() {
                String sortBy = "title";
                var direction = Sort.Direction.DESC;
                given(filmSpecs.titleContains(null)).willReturn(emptySpec());
                given(filmSpecs.releaseAfter(null)).willReturn(emptySpec());
                given(filmSpecs.releaseBefore(null)).willReturn(emptySpec());

                filmService.getFilms(1, 2, sortBy, direction, null, null, null);

                verify(filmRepository).findAll(ArgumentMatchers.<Specification<Film>>any(), pageableCaptor.capture());
                Sort sort = pageableCaptor.getValue().getSort();
                Sort.Order order = sort.getOrderFor(sortBy);
                assertThat(order).as("Sort order").isNotNull();
                assert order != null;
                assertThat(order.getDirection()).isEqualTo(direction);
            }

            @Test
            @DisplayName("Valid filter arguments, creates specifications correctly")
            void FilterArguments_CreatesSpecificationCorrectly() {
                String title = "island";
                LocalDate releaseAfter = LocalDate.of(2000, 2, 2);
                LocalDate releaseBefore = LocalDate.of(1000, 1, 1);
                given(filmSpecs.titleContains(title)).willReturn(emptySpec());
                given(filmSpecs.releaseAfter(releaseAfter)).willReturn(emptySpec());
                given(filmSpecs.releaseBefore(releaseBefore)).willReturn(emptySpec());

                filmService.getFilms(1, 2, null, null, title, releaseAfter, releaseBefore);

                verify(filmSpecs).titleContains(title);
                verify(filmSpecs).releaseAfter(releaseAfter);
                verify(filmSpecs).releaseBefore(releaseBefore);
            }
        }
    }

    @Nested
    @DisplayName("createFilm")
    class CreateFilm {

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.film.FilmServiceTests#validFilmInfoProvider")
        @DisplayName("Valid input, creates and returns")
        void ValidInput_Creates(String title, LocalDate releaseDate, String synopsis) {
            FilmInfo input = createFilmInfo(title, releaseDate, synopsis);
            when(filmRepository.save(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

            Film actual = filmService.createFilm(input);

            // assert saved
            var createdFilmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(createdFilmCaptor.capture());
            Film createdFilm = createdFilmCaptor.getValue();
            assertThat(createdFilm.getTitle()).isEqualTo(title);
            assertThat(createdFilm.getReleaseDate()).isEqualTo(releaseDate);
            assertThat(createdFilm.getSynopsis()).isEqualTo(synopsis);
            // assert returned
            assertThat(actual.getTitle()).isEqualTo(title);
            assertThat(actual.getReleaseDate()).isEqualTo(releaseDate);
            assertThat(actual.getSynopsis()).isEqualTo(synopsis);
        }
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

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.film.FilmServiceTests#validFilmInfoProvider")
        @DisplayName("Existing id, updates")
        void ExistingId_Updates(String title, LocalDate releaseDate, String synopsis) {
            final Long filmId = 1L;
            final Film existingFilm = createFilm(filmId, "Tenet", LocalDate.of(2020, 8, 26), "Armed with only the word \"Tenet\"");
            // find existing person
            given(filmRepository.findById(filmId)).willReturn(Optional.of(existingFilm));
            // return updated person
            when(filmRepository.save(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

            Film actual = filmService.updateFilm(filmId, createFilmInfo(title, releaseDate, synopsis));

            // assert saved
            var updatedFilmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(updatedFilmCaptor.capture());
            Film updatedFilm = updatedFilmCaptor.getValue();
            assertThat(updatedFilm.getId()).isEqualTo(filmId);
            assertThat(updatedFilm.getTitle()).isEqualTo(title);
            assertThat(updatedFilm.getReleaseDate()).isEqualTo(releaseDate);
            assertThat(updatedFilm.getSynopsis()).isEqualTo(synopsis);
            // assert returned
            assertThat(actual.getId()).isEqualTo(filmId);
            assertThat(actual.getTitle()).isEqualTo(title);
            assertThat(actual.getReleaseDate()).isEqualTo(releaseDate);
            assertThat(actual.getSynopsis()).isEqualTo(synopsis);
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

    @Nested
    @DisplayName("getCast")
    class getCast {

        @Test
        @DisplayName("Existing id, returns cast")
        void ExistingId_ReturnsCorrectly() {
            Film film = mock(Film.class);
            final Long filmId = 1L;
            final Role role = createRole(filmId, 2L, "Bane");
            when(film.getCast()).thenReturn(Set.of(role));
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));

            var actualCast = filmService.getCast(filmId);

            assertThatCollection(actualCast).containsExactly(role);
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        void NotExistingId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.getCast(1L)
            );
        }
    }

    @Nested
    @DisplayName("getDirectors")
    class getDirectors {

        @Test
        @DisplayName("Existing id, returns directors")
        void ExistingId_ReturnsCorrectly() {
            Film film = mock(Film.class);
            final Long filmId = 1L;
            final Person director = createPerson(2L);
            when(film.getDirectors()).thenReturn(Set.of(director));
            given(filmRepository.findById(filmId)).willReturn(Optional.of(film));

            var actualDirectors = filmService.getDirectors(filmId);

            assertThatCollection(actualDirectors).containsExactly(director);
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        void NotExistingId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    filmService.getDirectors(1L)
            );
        }
    }

    // Util

    private static Stream<Arguments> validFilmInfoProvider() {
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
