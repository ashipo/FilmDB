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
import org.springframework.transaction.annotation.Transactional;

import static com.demo.filmdb.graphql.Util.*;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Film Security")
public class FilmControllerSecurityTests {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName(FILMS)
    class Films {

        @Test
        @DisplayName("Not authenticated, authorized")
        void NotAuthenticated_Authorized() {
            graphQlTester.document("{films {id}}")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(GET_FILM)
    class GetFilm {

        @Test
        @DisplayName("Not authenticated, authorized")
        void NotAuthenticated_Authorized() {
            graphQlTester
                    .documentName(GET_FILM)
                    .variable(VAR_ID, 1)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(DELETE_FILM)
    class DeleteFilm {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable(VAR_ID, -1)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable(VAR_ID, -1)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == FORBIDDEN);
        }

        @Test
        @DisplayName("Authenticated as ADMIN, authorized")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void AuthenticatedAdmin_Authorized() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable(VAR_ID, 1)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(CREATE_FILM)
    class CreateFilm {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable(TITLE, "The Godfather")
                    .variable(RELEASE_DATE, "1972-03-14")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable(TITLE, "The Godfather Part II")
                    .variable(RELEASE_DATE, "1974-12-12")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == FORBIDDEN);
        }

        @Test
        @DisplayName("Authenticated as ADMIN, authorized")
        @WithMockUser(roles = {ROLE_ADMIN})
        @DirtiesContext
        void AuthenticatedAdmin_Authorized() {
            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable(TITLE, "The Godfather Part III")
                    .variable(RELEASE_DATE, "1990-12-20")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(UPDATE_FILM)
    class UpdateFilm {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, -1)
                    .variable(TITLE, "La Femme Nikita")
                    .variable(RELEASE_DATE, "1990-02-21")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, -1)
                    .variable(TITLE, "The Fifth Element")
                    .variable(RELEASE_DATE, "1997-05-07")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == FORBIDDEN);
        }

        @Test
        @DisplayName("Authenticated as ADMIN, authorized")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void AuthenticatedAdmin_Authorized() {
            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable(VAR_ID, 1)
                    .variable(TITLE, "Léon: The Professional")
                    .variable(RELEASE_DATE, "1994-09-14")
                    .executeAndVerify();
        }
    }
}
