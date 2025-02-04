package com.demo.filmdb.graphql.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static com.demo.filmdb.graphql.Util.*;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static graphql.ErrorType.ValidationError;

/**
 * Domain level constraints validation test
 */
@SpringBootTest
@DisplayName("GraphQL Role domain constraints validation")
public class RoleIntegrationTests {

    private WebGraphQlTester graphQlTester;

    @Autowired
    @SuppressWarnings("unused")
    public void setWebGraphHandler(WebGraphQlHandler handler) {
        graphQlTester = WebGraphQlTester.create(handler);
    }

    @Nested
    @DisplayName(CREATE_ROLE)
    class CreateRole {

        @Test
        @DisplayName("Valid character field length (<= 255), correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void ValidInput_CorrectResponse() {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, 1)
                    .variable(PERSON_ID, 4)
                    .variable(CHARACTER, "A".repeat(255))
                    .execute()
                    .path(CREATE_ROLE + ".role")
                    .hasValue();
        }

        @Test
        @DisplayName("Invalid character field length (> 255), validation error")
        @WithMockUser(roles = {ROLE_ADMIN})
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, 1)
                    .variable(PERSON_ID, 4)
                    .variable(CHARACTER, "A".repeat(256))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }
}
