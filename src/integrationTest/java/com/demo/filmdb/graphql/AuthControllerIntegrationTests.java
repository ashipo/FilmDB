package com.demo.filmdb.graphql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Objects;

@SpringBootTest
@AutoConfigureGraphQlTester
@DisplayName("GraphQL Authentication")
public class AuthControllerIntegrationTests {
    @Autowired
    GraphQlTester graphQlTester;

    @Test
    @DisplayName("login: valid credentials, response contains a token")
    void ValidCredentials_ResponseContainsToken() {
        graphQlTester
                .documentName("login")
                .variable("username", "user")
                .variable("password", "password")
                .execute()
                .path("login")
                .hasValue();
    }

    @Test
    @DisplayName("login: invalid credentials, response contains error")
    void ValidCredentials_ResponseContainsError() {
        graphQlTester
                .documentName("login")
                .variable("username", "not a user")
                .variable("password", "not a password")
                .execute()
                .errors()
                .expect(responseError -> Objects.requireNonNull(responseError.getMessage()).contains("Invalid credentials"));
    }
}
