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

import static com.demo.filmdb.graphql.Util.*;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Role Security")
public class RoleControllerSecurityTests {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName(CREATE_ROLE)
    class CreateRole {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 1L)
                    .variable(CHARACTER, "Merry")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .variable(CHARACTER, "Pippin")
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
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 3L)
                    .variable(CHARACTER, "Sam")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(GET_ROLE)
    class GetRole {

        @Test
        @DisplayName("Not authenticated, authorized")
        void NotAuthenticated_Authorized() {
            graphQlTester
                    .documentName(GET_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 1L)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(UPDATE_ROLE)
    class UpdateRole {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(UPDATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 1L)
                    .variable(CHARACTER, "Merry")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(UPDATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .variable(CHARACTER, "Pippin")
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
                    .documentName(UPDATE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .variable(CHARACTER, "Sam")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(DELETE_ROLE)
    class DeleteRole {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(DELETE_ROLE)
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
                    .documentName(DELETE_ROLE)
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
                    .documentName(DELETE_ROLE)
                    .variable(FILM_ID, 1L)
                    .variable(PERSON_ID, 2L)
                    .executeAndVerify();
        }

    }

    @Nested
    @DisplayName(UPDATE_CAST)
    class UpdateCast {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(UPDATE_CAST)
                    .variable(FILM_ID, 1L)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(UPDATE_CAST)
                    .variable(FILM_ID, 1L)
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
                    .documentName(UPDATE_CAST)
                    .variable(FILM_ID, 1L)
                    .executeAndVerify();
        }
    }
}
