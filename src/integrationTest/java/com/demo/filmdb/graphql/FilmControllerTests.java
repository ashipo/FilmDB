package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.inputs.FilmInput;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@GraphQlTest({FilmController.class, TestConfigurer.class})
@DisplayName("GraphQL Film")
public class FilmControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    private FilmService filmService;

    @Nested
    @DisplayName(FILMS)
    class Films {

        @Test
        @DisplayName("No arguments, runs successfully")
        void NoArguments_RunsSuccessfully() {
            graphQlTester
                    .documentName(FILMS)
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Arbitrary paging, requests correct page")
        void ArbitraryPaging_RequestsCorrectPage() {
            final Integer pageNumber = 3;
            final Integer pageSize = 14;

            graphQlTester
                    .documentName(FILMS)
                    .variable("page", pageNumber)
                    .variable("pageSize", pageSize)
                    .executeAndVerify();

            ArgumentCaptor<Pageable> requestedPageable = ArgumentCaptor.forClass(Pageable.class);
            verify(filmService).getAllFilms(requestedPageable.capture());
            assertThat(requestedPageable.getValue().getPageNumber()).isEqualTo(pageNumber);
            assertThat(requestedPageable.getValue().getPageSize()).isEqualTo(pageSize);
        }

        @Test
        @DisplayName("Arbitrary paging, correct response size")
        void CustomPaging_RequestsCorrectPage() {
            final int pageSize = 5;
            List<Film> films = new ArrayList<>(pageSize);
            for (int i = 0; i < pageSize; i++) {
                films.add(new Film());
            }
            Page<Film> filmsPage = new PageImpl<>(films);
            given(filmService.getAllFilms(any())).willReturn(filmsPage);

            graphQlTester
                    .documentName(FILMS)
                    .variable("pageSize", pageSize)
                    .execute()
                    .path(FILMS)
                    .entityList(Film.class)
                    .hasSize(pageSize);
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidArgument_ValidationError() {
            graphQlTester
                    .documentName(FILMS)
                    .variable("page", "one")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(FILM_BY_ID)
    class FilmById {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            Long id = 37L;
            Film expectedFilm = new Film("title", LocalDate.now(), "synopsis");
            expectedFilm.setId(id);
            given(filmService.getFilm(anyLong())).willReturn(expectedFilm);

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", id)
                    .execute()
                    .path(FILM_BY_ID)
                    .entity(Film.class)
                    .matches(film -> film.getId().equals(id))
                    .matches(film -> film.getTitle().equals(expectedFilm.getTitle()))
                    .matches(film -> film.getReleaseDate().equals(expectedFilm.getReleaseDate()))
                    .matches(film -> film.getSynopsis().equals(expectedFilm.getSynopsis()));
        }

        @Test
        @DisplayName("Non existing id, null response")
        void NotExistingId_NullResponse() {
            given(filmService.getFilm(anyLong())).willReturn(null);

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", 1)
                    .execute()
                    .path(FILM_BY_ID)
                    .valueIsNull();
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            given(filmService.getFilm(anyLong())).willReturn(new Film());

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", "Invalid ID")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(DELETE_FILM)
    class DeleteFilm {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            Long expectedId = 1L;

            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable("id", expectedId)
                    .execute()
                    .path(DELETE_FILM)
                    .entity(Long.class).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable("id", "five")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }

        @Test
        @DisplayName("Missing input, validation error")
        void MissingInput_ValidationError() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(CREATE_FILM)
    class CreateFilm {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#validFilmInputs")
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse(FilmInput input) {
            Map<String, Object> filmInput = getFilmInputMap(input);
            when(filmService.saveFilm(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable("filmInput", filmInput)
                    .execute()
                    .path(CREATE_FILM)
                    .entity(Film.class)
                    .matches(film -> Objects.equals(film.getTitle(), input.title()))
                    .matches(film -> Objects.equals(film.getReleaseDate(), input.releaseDate()))
                    .matches(film -> Objects.equals(film.getSynopsis(), input.synopsis()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#invalidFilmInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(FilmInput input) {
            Map<String, Object> filmInput = getFilmInputMap(input);

            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable("filmInput", filmInput)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_FILM)
    class UpdateFilm {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#validFilmInputs")
        @DisplayName("Valid input, saves correctly")
        void ValidInput_SavesCorrectly(FilmInput input) {
            Map<String, Object> filmInput = getFilmInputMap(input);
            Long expectedId = 1L;
            when(filmService.updateFilm(any(Film.class))).then(AdditionalAnswers.returnsFirstArg());

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable("id", expectedId)
                    .variable("filmInput", filmInput)
                    .execute()
                    .path(UPDATE_FILM)
                    .entity(Film.class)
                    .matches(film -> Objects.equals(film.getId(), expectedId))
                    .matches(film -> Objects.equals(film.getTitle(), input.title()))
                    .matches(film -> Objects.equals(film.getReleaseDate(), input.releaseDate()))
                    .matches(film -> Objects.equals(film.getSynopsis(), input.synopsis()));
        }

        @Test
        @DisplayName("Not existing id, not found error")
        void NotExistingId_NotFoundError() {
            Map<String, Object> filmInput = getFilmInputMap(getValidFilmInput());
            given(filmService.updateFilm(any(Film.class))).willThrow(new EntityNotFoundException("404"));

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable("id", 1L)
                    .variable("filmInput", filmInput)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(UPDATE_FILM)
                    .valueIsNull();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#invalidFilmInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(FilmInput input) {
            Map<String, Object> filmInput = getFilmInputMap(input);

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable("id", 1)
                    .variable("filmInput", filmInput)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_DIRECTORS)
    class UpdateDirectors {

        @Captor
        ArgumentCaptor<List<Long>> idsCaptor;

        @Test
        @DisplayName("Valid input, updates correctly")
        void ValidInput_CorrectResponse() {
            Long filmId = 5L;
            List<Long> directorsIds = List.of(3L, 7L);

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", filmId)
                    .variable("directorsIds", directorsIds)
                    .executeAndVerify();

            ArgumentCaptor<Long> filmIdCaptor = ArgumentCaptor.forClass(Long.class);
            verify(filmService).updateDirectors(filmIdCaptor.capture(), idsCaptor.capture());
            assertThat(filmIdCaptor.getValue()).isEqualTo(filmId);
            assertThat(idsCaptor.getValue()).isEqualTo(directorsIds);
        }

        @Test
        @DisplayName("Not existing ids, not found error")
        void NotExistingIds_NotFoundError() {
            given(filmService.updateDirectors(anyLong(), anyCollection())).willThrow(new EntityNotFoundException("Msg"));

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 5L)
                    .variable("directorsIds", List.of(3L))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(UPDATE_DIRECTORS)
                    .valueIsNull();
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("directorsIds", List.of(3L))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    private static Stream<Arguments> validFilmInputs() {
        final String title = "Forrest Gump";
        final LocalDate date = LocalDate.now();
        final String synopsis = "Life was like a box of chocolates";
        return Stream.of(
                arguments(named("All fields", new FilmInput(title, date, synopsis))),
                arguments(named("Null synopsis", new FilmInput(title, date, null)))
        );
    }

    private static Stream<Arguments> invalidFilmInputs() {
        final String title = "Alien";
        final LocalDate date = LocalDate.now();
        final String synopsis = "In space, no can hear you scream";
        return Stream.of(
                arguments(named("Null title", new FilmInput(null, date, synopsis))),
                arguments(named("Null release date", new FilmInput(title, null, synopsis))),
                arguments(named("Empty title", new FilmInput("", date, synopsis)))
        );
    }
}
