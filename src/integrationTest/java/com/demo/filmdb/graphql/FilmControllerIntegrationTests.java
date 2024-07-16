package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.graphql.inputs.FilmInput;
import com.demo.filmdb.person.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.demo.filmdb.Utils.NOT_EXISTING_ID;
import static com.demo.filmdb.Utils.ROLE_ADMIN;
import static graphql.ErrorType.ValidationError;
import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Film")
class FilmControllerIntegrationTests {

    private final String UPDATE_DIRECTORS = "updateFilmDirectors";

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName("films")
    class Films {

        @Test
        @DisplayName("Page 0, size 20, response contains all (3) films")
        void GetAllFilms_CorrectResponse() {
            graphQlTester
                    .documentName("films")
                    .variable("page", "0")
                    .variable("pageSize", "20")
                    .execute()
                    .path("films")
                    .entityList(Film.class)
                    .hasSize(3);
        }

        @Test
        @DisplayName("Page 1, size 2, response contains 1 film")
        void GetPage_CorrectResponse() {
            graphQlTester
                    .documentName("films")
                    .variable("page", "1")
                    .variable("pageSize", "2")
                    .execute()
                    .path("films")
                    .entityList(Film.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("No arguments, runs successfully")
        void NoArguments_RunsSuccessfully() {
            graphQlTester
                    .documentName("films")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName("filmById")
    class FilmById {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            graphQlTester
                    .documentName("filmById")
                    .variable("id", "1")
                    .execute()
                    .path("filmById")
                    .matchesJson("""
                                {
                                    "title": "Thor: Ragnarok",
                                    "synopsis": "Imprisoned on the planet Sakaar, Thor must race against time to return to Asgard and stop Ragnarök, the destruction of his world, at the hands of the powerful and ruthless  villain Hela.",
                                    "releaseDate": "2017-10-10"
                                }
                            """);
        }

        @Test
        @DisplayName("Non existing id, null response")
        void NotExistingId_ResponseNull() {
            graphQlTester
                    .documentName("filmById")
                    .variable("id", NOT_EXISTING_ID)
                    .execute()
                    .path("filmById")
                    .valueIsNull();
        }
    }

    @Nested
    @DisplayName("deleteFilm")
    class DeleteFilm {

        @Test
        @DisplayName("Authorized, response has no errors")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void Authorized_NoErrors() {
            graphQlTester
                    .documentName("deleteFilm")
                    .variable("id", "1")
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Authorized, response value is correct")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void Authorized_CorrectValue() {
            String expectedId = "1";
            graphQlTester
                    .documentName("deleteFilm")
                    .variable("id", expectedId)
                    .execute()
                    .path("deleteFilm")
                    .entity(String.class).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("Not authorized, response contains UNAUTHORIZED error")
        void NotAuthorized_UnauthorizedError() {
            graphQlTester
                    .documentName("deleteFilm")
                    .variable("id", "1")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Not authorized, response value is null")
        void NotAuthorized_NullValue() {
            graphQlTester
                    .documentName("deleteFilm")
                    .variable("id", "1")
                    .execute()
                    .errors()
                    .filter(e -> true)
                    .verify()
                    .path("deleteFilm")
                    .valueIsNull();
        }
    }

    @Nested
    @DisplayName("createFilm")
    class CreateFilm {

        @Test
        @DisplayName("Valid mutation, correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void ValidMutation_CorrectResponse() {
            FilmInput input = getValidFilmInput();
            graphQlTester
                    .documentName("createFilm")
                    .variable("filmInput", input)
                    .execute()
                    .path("createFilm")
                    .entity(Film.class)
                    .matches(film -> film.getTitle().equals(input.title()))
                    .matches(film -> film.getReleaseDate().equals(input.releaseDate()))
                    .matches(film -> film.getSynopsis().equals(input.synopsis()));
        }

        @Test
        @DisplayName("Invalid mutation, validation error")
        @WithMockUser(roles = {ROLE_ADMIN})
        void InvalidMutation_NullResponse() {
            FilmInput input = new FilmInput("", null, "There is a mission.");
            graphQlTester
                    .documentName("createFilm")
                    .variable("filmInput", input)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError);
        }

        @Test
        @DisplayName("Not authorized, valid input, response contains UNAUTHORIZED error")
        void NotAuthorized_UnauthorizedError() {
            graphQlTester
                    .documentName("createFilm")
                    .variable("filmInput", getValidFilmInput())
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("updateFilm")
    class UpdateFilm {

        @Test
        @DisplayName("Existing ID, Valid mutation, correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void ExistingIdValidMutation_CorrectResponse() {
            FilmInput input = getValidFilmInput();
            Long id = 1L;
            graphQlTester
                    .documentName("updateFilm")
                    .variable("id", id)
                    .variable("filmInput", input)
                    .execute()
                    .path("updateFilm")
                    .entity(Film.class)
                    .matches(film -> film.getId().equals(id))
                    .matches(film -> film.getTitle().equals(input.title()))
                    .matches(film -> film.getReleaseDate().equals(input.releaseDate()))
                    .matches(film -> film.getSynopsis().equals(input.synopsis()));
        }

        @Test
        @DisplayName("Not existing ID, Valid mutation, null response")
        @WithMockUser(roles = {ROLE_ADMIN})
        void NotExistingIdValidMutation_NullResponse() {
            graphQlTester
                    .documentName("updateFilm")
                    .variable("id", NOT_EXISTING_ID)
                    .variable("filmInput", getValidFilmInput())
                    .execute()
                    .path("updateFilm")
                    .valueIsNull();
        }

        @Test
        @DisplayName("Not authorized, valid input, response contains UNAUTHORIZED error")
        void NotAuthorized_UnauthorizedError() {
            graphQlTester
                    .documentName("updateFilm")
                    .variable("id", 1L)
                    .variable("filmInput", getValidFilmInput())
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName(UPDATE_DIRECTORS)
    class UpdateFilmDirectors {

        @Test
        @DisplayName("Existing IDs, correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        void ExistingIds_CorrectResponse() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1L)
                    .variable("directorsIds", List.of(2L, 3L, 4L))
                    .execute()
                    .path(UPDATE_DIRECTORS + ".directors")
                    .entityList(Person.class)
                    .hasSize(3);
        }

        @Test
        @DisplayName("Existing filmId, null directorsIds, directors removed")
        @WithMockUser(roles = {ROLE_ADMIN})
        void NullDirectorsIds_RemovesDirectors() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1L)
                    .execute()
                    .path(UPDATE_DIRECTORS + ".directors")
                    .entityList(Person.class)
                    .hasSize(0);
        }

        @Test
        @DisplayName("Not existing filmId, null response")
        @WithMockUser(roles = {ROLE_ADMIN})
        void NullDirectorsIds_ResponseNull() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", NOT_EXISTING_ID)
                    .execute()
                    .path(UPDATE_DIRECTORS)
                    .valueIsNull();
        }

        @Test
        @DisplayName("Not authorized, response contains UNAUTHORIZED error")
        void NotAuthorized_UnauthorizedError() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1L)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }
    }

    private FilmInput getValidFilmInput() {
        return new FilmInput("Mission: Impossible", LocalDate.now(), "There is a mission.");
    }
}
