package com.demo.filmdb.graphql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Film")
class FilmControllerIntegrationTests {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName("filmById")
    class FilmById {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            graphQlTester
                    .documentName("filmDetails")
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
                    .documentName("filmDetails")
                    .variable("id", "100")
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
        @WithMockUser(roles = {"ADMIN"})
        @DirtiesContext
        void Authorized_NoErrors() {
            graphQlTester
                    .documentName("deleteFilm")
                    .variable("id", "1")
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Authorized, response value is correct")
        @WithMockUser(roles = {"ADMIN"})
        @DirtiesContext
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
}
