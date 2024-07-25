package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.graphql.inputs.RoleInput;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Objects;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;

@GraphQlTest({RoleController.class, TestConfigurer.class})
@DisplayName("GraphQL Role")
public class RoleControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    private RoleService roleService;

    @Nested
    @DisplayName(CREATE_ROLE)
    class CreateRole {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long filmId = 1L;
            final Long personId = 4L;
            final String character = "Frodo";
            Film film = createFilm(filmId);
            Person person = createPerson(personId);
            given(roleService.createRole(filmId, personId, character)).willReturn(new Role(film, person, character));

            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable("filmId", filmId)
                    .variable("personId", personId)
                    .variable("character", character)
                    .execute()
                    .path(CREATE_ROLE)
                    .entity(Role.class)
                    .matches(role -> Objects.equals(role.getFilm().getId(), filmId))
                    .matches(role -> Objects.equals(role.getPerson().getId(), personId))
                    .matches(role -> Objects.equals(role.getCharacter(), character));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.graphql.RoleControllerTests#invalidRoleInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(RoleInput input) {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable("filmId", input.filmId())
                    .variable("personId", input.personId())
                    .variable("character", input.character())
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    private static Stream<Arguments> invalidRoleInputs() {
        final String character = "Boromir";
        return Stream.of(
                arguments(named("Null film id", new RoleInput(null, 1L, character))),
                arguments(named("Null person id", new RoleInput(1L, null, character))),
                arguments(named("Null character", new RoleInput(1L, 1L, null))),
                arguments(named("Empty character", new RoleInput(1L, 1L, "")))
        );
    }
}
