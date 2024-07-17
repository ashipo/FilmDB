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

import java.util.List;

import static com.demo.filmdb.Utils.ROLE_ADMIN;
import static com.demo.filmdb.graphql.Util.*;
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
            graphQlTester
                    .documentName(FILMS)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(FILM_BY_ID)
    class FilmById {

        @Test
        @DisplayName("Not authenticated, authorized")
        void NotAuthenticated_Authorized() {
            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", 1)
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
                    .variable("id", 1)
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
                    .variable("id", 1)
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
                    .variable("id", 1)
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
                    .variable("filmInput", getValidFilmInput())
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
                    .variable("filmInput", getValidFilmInput())
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
                    .variable("filmInput", getValidFilmInput())
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
                    .variable("id", 1)
                    .variable("filmInput", getValidFilmInput())
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
                    .variable("id", 1)
                    .variable("filmInput", getValidFilmInput())
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
                    .variable("id", 1)
                    .variable("filmInput", getValidFilmInput())
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(UPDATE_DIRECTORS)
    class UpdateFilmDirectors {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1)
                    .variable("directorsIds", List.of(3L))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1)
                    .variable("directorsIds", List.of(3L))
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
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 1)
                    .variable("directorsIds", List.of(3L))
                    .executeAndVerify();
        }
    }
}
