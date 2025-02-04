package com.demo.filmdb.graphql.person;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.demo.filmdb.graphql.Util.*;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static graphql.ErrorType.ValidationError;

/**
 * Domain level constraints validation test
 */
@SpringBootTest
@DisplayName("GraphQL Person domain constraints validation")
public class PersonIntegrationTests {

    private WebGraphQlTester graphQlTester;

    @Autowired
    @SuppressWarnings("unused")
    public void setWebGraphHandler(WebGraphQlHandler handler) {
        graphQlTester = WebGraphQlTester.create(handler);
    }

    @Nested
    @DisplayName(CREATE_PERSON)
    class CreatePerson {

        @Test
        @DisplayName("Valid input, correct response")
        @WithMockUser(roles = {ROLE_ADMIN})
        @Transactional
        void ValidInput_CorrectResponse() {
            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, "A".repeat(255))
                    .variable(DATE_OF_BIRTH, null)
                    .execute()
                    .path(CREATE_PERSON + ".person")
                    .hasValue();
        }

        @Test
        @DisplayName("Invalid input, validation error")
        @WithMockUser(roles = {ROLE_ADMIN})
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, "A".repeat(256))
                    .variable(DATE_OF_BIRTH, LocalDate.of(1969, 11, 4))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }
}
