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

import static com.demo.filmdb.Utils.ROLE_ADMIN;
import static com.demo.filmdb.graphql.Util.*;
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
                    .variable("filmId", 1L)
                    .variable("personId", 1L)
                    .variable("character", "Merry")
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
                    .variable("filmId", 1L)
                    .variable("personId", 2L)
                    .variable("character", "Pippin")
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
                    .variable("filmId", 1L)
                    .variable("personId", 3L)
                    .variable("character", "Sam")
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
}
