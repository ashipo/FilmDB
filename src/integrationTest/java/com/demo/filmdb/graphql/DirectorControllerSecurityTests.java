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

import static com.demo.filmdb.Utils.NOT_EXISTING_ID;
import static com.demo.filmdb.Utils.ROLE_ADMIN;
import static com.demo.filmdb.graphql.Util.*;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Director Security")
public class DirectorControllerSecurityTests {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName(SET_DIRECTOR)
    class SetDirector {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(SET_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(SET_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
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
                    .documentName(SET_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 3L)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(DELETE_DIRECTOR)
    class DeleteDirector {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(DELETE_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(DELETE_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
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
                    .documentName(DELETE_DIRECTOR)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
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
                    .variable(FILM_ID, NOT_EXISTING_ID)
                    .variable(DIRECTORS_IDS, List.of(NOT_EXISTING_ID))
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
                    .variable(FILM_ID, NOT_EXISTING_ID)
                    .variable(DIRECTORS_IDS, List.of(NOT_EXISTING_ID))
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
                    .variable(FILM_ID, 1)
                    .variable(DIRECTORS_IDS, List.of(3L))
                    .executeAndVerify();
        }
    }
}
