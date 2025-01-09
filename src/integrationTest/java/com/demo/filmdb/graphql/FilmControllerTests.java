package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.enums.SortableFilmField;
import com.demo.filmdb.graphql.payloads.DeleteFilmPayload;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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
            graphQlTester.document("{films {id}}")
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Valid input, correct FilmService call")
        void ValidInput_CorrectServiceCall() {
            int page = 3;
            int pageSize = 14;
            var sortBy = SortableFilmField.TITLE;
            var sortDirection = Sort.Direction.DESC;
            var title = "peace";
            var releaseBefore = LocalDate.of(1950, 11, 11);

            graphQlTester.documentName(FILMS)
                    .variable(PAGE, page)
                    .variable(PAGE_SIZE, pageSize)
                    .variable(SORT_BY, sortBy)
                    .variable(SORT_DIRECTION, sortDirection)
                    .variable(TITLE, title)
                    .variable("releaseBefore", releaseBefore)
                    .executeAndVerify();

            verify(filmService).getFilms(
                    page,
                    pageSize,
                    sortBy.getFieldName(),
                    sortDirection,
                    title,
                    null,
                    releaseBefore
            );
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#invalidFilmsInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(
                Object page,
                Object pageSize,
                Object sortBy,
                Object sortDirection,
                Object title,
                Object releaseAfter,
                Object releaseBefore
        ) {
            graphQlTester.documentName(FILMS)
                    .variable(PAGE, page)
                    .variable(PAGE_SIZE, pageSize)
                    .variable(SORT_BY, sortBy)
                    .variable(SORT_DIRECTION, sortDirection)
                    .variable(TITLE, title)
                    .variable("releaseAfter", releaseAfter)
                    .variable("releaseBefore", releaseBefore)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(GET_FILM)
    class GetFilm {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            Long id = 37L;
            Film expectedFilm = createFilm(id);
            given(filmService.getFilm(anyLong())).willReturn(Optional.of(expectedFilm));

            graphQlTester
                    .documentName(GET_FILM)
                    .variable(VAR_ID, id)
                    .execute()
                    .path(GET_FILM)
                    .entity(Film.class)
                    .matches(film -> Objects.equals(film.getId(), id))
                    .matches(film -> Objects.equals(film.getTitle(), expectedFilm.getTitle()))
                    .matches(film -> Objects.equals(film.getReleaseDate(), expectedFilm.getReleaseDate()))
                    .matches(film -> Objects.equals(film.getSynopsis(), expectedFilm.getSynopsis()));
        }

        @Test
        @DisplayName("Non existing id, null response")
        void NotExistingId_NullResponse() {
            given(filmService.getFilm(anyLong())).willReturn(Optional.empty());

            graphQlTester
                    .documentName(GET_FILM)
                    .variable(VAR_ID, 1)
                    .execute()
                    .path(GET_FILM)
                    .valueIsNull();
        }

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        // Valid: "query { film(id: 1) { id } }"
        @ValueSource(strings = {
                "query { film(id: 1) }",
                "query { film { id } }",
                "query { film(id: null) { id } }",
                "query { film(id: \"one\") { id } }"
        })
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(String document) {
            graphQlTester
                    .document(document)
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
                    .variable(VAR_ID, expectedId)
                    .execute()
                    .path(DELETE_FILM)
                    .entity(DeleteFilmPayload.class)
                    .matches(payload -> Objects.equals(payload.id(), expectedId));
        }

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        // Valid: "mutation { deleteFilm(input: {id: 1}) { id } }"
        @ValueSource(strings = {
                "mutation { deleteFilm(input: {id: 1}) }",
                "mutation { deleteFilm(input: null) { id } }",
                "mutation { deleteFilm(input: {}) { id } }",
                "mutation { deleteFilm(input: {id: null}) { id } }",
                "mutation { deleteFilm(input: {id: \"one\"}) { id } }",
        })
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(String document) {
            graphQlTester
                    .document(document)
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

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#validFilmInputs")
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse(String title, LocalDate releaseDate, String synopsis) {
            given(filmService.createFilm(any())).willReturn(new Film(1L, title, releaseDate, synopsis));

            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable(TITLE, title)
                    .variable(RELEASE_DATE, releaseDate)
                    .variable(SYNOPSIS, synopsis)
                    .execute()
                    .path(CREATE_FILM + ".film")
                    .entity(Film.class)
                    .matches(film -> Objects.equals(film.getTitle(), title))
                    .matches(film -> Objects.equals(film.getReleaseDate(), releaseDate))
                    .matches(film -> Objects.equals(film.getSynopsis(), synopsis));
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#invalidFilmInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object title, Object releaseDate, Object synopsis) {
            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable(TITLE, title)
                    .variable(RELEASE_DATE, releaseDate)
                    .variable(SYNOPSIS, synopsis)
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

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#validFilmInputs")
        @DisplayName("Valid input, correct response")
        void ValidInput_SavesCorrectly(String title, LocalDate releaseDate, String synopsis) {
            final Long id = 1L;
            given(filmService.updateFilm(anyLong(), any())).willReturn(new Film(id, title, releaseDate, synopsis));

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, id)
                    .variable(TITLE, title)
                    .variable(RELEASE_DATE, releaseDate)
                    .variable(SYNOPSIS, synopsis)
                    .execute()
                    .path(UPDATE_FILM + ".film")
                    .entity(Film.class)
                    .matches(film -> Objects.equals(film.getId(), id))
                    .matches(film -> Objects.equals(film.getTitle(), title))
                    .matches(film -> Objects.equals(film.getReleaseDate(), releaseDate))
                    .matches(film -> Objects.equals(film.getSynopsis(), synopsis));
        }

        @Test
        @DisplayName("Not existing id, not found error")
        void NotExistingId_NotFoundError() {
            given(filmService.updateFilm(anyLong(), any())).willThrow(new EntityNotFoundException("404"));

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, 1)
                    .variable(TITLE, "The Truman Show")
                    .variable(RELEASE_DATE, "1998-06-01")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(UPDATE_FILM)
                    .valueIsNull();
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.FilmControllerTests#invalidFilmInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object title, Object releaseDate, Object synopsis) {
            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, 1)
                    .variable(TITLE, title)
                    .variable(RELEASE_DATE, releaseDate)
                    .variable(SYNOPSIS, synopsis)
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
        final LocalDate date = LocalDate.of(1994, 7, 6);
        final String synopsis = "Life was like a box of chocolates";
        return Stream.of(
                arguments(title, date, synopsis),
                arguments(title, date, NULL)
        );
    }

    private static Stream<Arguments> invalidFilmInputs() {
        final String title = "Alien";
        final LocalDate date = LocalDate.of(1979, 6, 22);
        final String synopsis = "In space, no one can hear you scream";
        return Stream.of(
                arguments(NULL, date, synopsis),
                arguments(EMPTY_STRING, date, synopsis),
                arguments(BLANK_STRING, date, synopsis),
                arguments(title, NULL, synopsis),
                arguments(title, INVALID_DATE, synopsis)
        );
    }

    private static Stream<Arguments> invalidFilmsInputs() {
        int page = 3;
        int pageSize = 14;
        var sortBy = SortableFilmField.RELEASE_DATE;
        var sortDirection = Sort.Direction.DESC;
        var title = "Alien";
        var releaseAfter = LocalDate.of(1950, 10, 10);
        var releaseBefore = LocalDate.of(1990, 11, 11);
        return Stream.of(
                arguments(NULL, pageSize, sortBy, sortDirection, title, releaseAfter, releaseBefore),
                arguments(page, NULL, sortBy, sortDirection, title, releaseAfter, releaseBefore),
                arguments(page, pageSize, sortBy, sortDirection, BLANK_STRING, releaseAfter, releaseBefore),
                arguments(page, pageSize, sortBy, sortDirection, title, INVALID_DATE, releaseBefore),
                arguments(page, pageSize, sortBy, sortDirection, title, releaseAfter, INVALID_DATE)
        );
    }
}
