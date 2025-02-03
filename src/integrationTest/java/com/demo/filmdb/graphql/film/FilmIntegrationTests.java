package com.demo.filmdb.graphql.film;

import com.demo.filmdb.film.Film;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static graphql.ErrorType.ValidationError;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Domain level constraints validation test
 */
@SpringBootTest
@DisplayName("GraphQL Film domain constraints validation")
public class FilmIntegrationTests {

    private WebGraphQlTester graphQlTester;

    @Autowired
    @SuppressWarnings("unused")
    public void setWebGraphHandler(WebGraphQlHandler handler) {
        graphQlTester = WebGraphQlTester.create(handler);
    }

    @Nested
    @DisplayName(CREATE_FILM)
    class CreateFilm {

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.film.FilmIntegrationTests#validFilmInputs")
        @DisplayName("Valid input, correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void ValidInput_CorrectResponse(Object title, Object releaseDate, Object synopsis) {
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
        @MethodSource("com.demo.filmdb.graphql.film.FilmIntegrationTests#invalidFilmInputs")
        @DisplayName("Invalid input, validation error")
        @WithMockUser(roles = {ROLE_ADMIN})
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

    private static Stream<Arguments> validFilmInputs() {
        final String title = "Alien";
        final LocalDate date = LocalDate.of(1979, 6, 22);
        final String synopsis = "In space, no one can hear you scream";
        return Stream.of(
                arguments(stringOfLength(255), date, synopsis),
                arguments(title, date, stringOfLength(2000))
        );
    }

    private static Stream<Arguments> invalidFilmInputs() {
        final String title = "Alien";
        final LocalDate date = LocalDate.of(1979, 6, 22);
        final String synopsis = "In space, no one can hear you scream";
        return Stream.of(
                arguments(stringOfLength(256), date, synopsis),
                arguments(title, date, stringOfLength(2001))
        );
    }
}
