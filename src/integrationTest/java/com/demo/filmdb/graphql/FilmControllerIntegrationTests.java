package com.demo.filmdb.graphql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureGraphQlTester
class FilmControllerIntegrationTests {

    @Autowired
    GraphQlTester graphQlTester;

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
        @DisplayName("Authorized, no errors")
        @WithMockUser(roles = {"ADMIN"})
        @DirtiesContext
        void Authorized_NoErrors() {
            graphQlTester
                    .documentName("deleteFilm")
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Not authorized, response contains error")
        void NotAuthorized_UnauthorizedError() {
            graphQlTester
                    .documentName("deleteFilm")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }
    }
}
