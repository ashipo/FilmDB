package com.demo.filmdb.graphql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static com.demo.filmdb.Utils.ROLE_ADMIN;
import static com.demo.filmdb.graphql.Util.*;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.UNAUTHORIZED;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@DisplayName("GraphQL Person Security")
public class PersonControllerSecurityTests {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Nested
    @DisplayName(CREATE_PERSON)
    class CreatePerson {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, "Anthony Hopkins")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, "Jodie Foster")
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
                    .documentName(CREATE_PERSON)
                    .variable(NAME, "Scott Glenn")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(GET_PERSON)
    class GetPerson {

        @Test
        @DisplayName("Not authenticated, authorized")
        void NotAuthenticated_Authorized() {
            graphQlTester
                    .documentName(GET_PERSON)
                    .variable("id", 1)
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(UPDATE_PERSON)
    class UpdatePerson {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(VAR_ID, 6)
                    .variable(NAME, "Chris Tucker")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(VAR_ID, 7)
                    .variable(NAME, "Ian Holm")
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
                    .documentName(UPDATE_PERSON)
                    .variable(VAR_ID, 3)
                    .variable(NAME, "Luke Perry")
                    .executeAndVerify();
        }
    }

    @Nested
    @DisplayName(DELETE_PERSON)
    class DeletePerson {

        @Test
        @DisplayName("Not authenticated, unauthorized")
        void NotAuthenticated_Unauthorized() {
            graphQlTester
                    .documentName(DELETE_PERSON)
                    .variable(VAR_ID, 1)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == UNAUTHORIZED);
        }

        @Test
        @DisplayName("Authenticated as USER, forbidden")
        @WithMockUser
        void AuthenticatedUser_Forbidden() {
            graphQlTester
                    .documentName(DELETE_PERSON)
                    .variable(VAR_ID, 1)
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
                    .documentName(DELETE_PERSON)
                    .variable(VAR_ID, 1)
                    .executeAndVerify();
        }
    }
}
