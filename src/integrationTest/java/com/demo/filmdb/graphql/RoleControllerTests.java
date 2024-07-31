package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.payloads.DeleteRolePayload;
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
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
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
            final Role expectedRole = createRole(filmId, personId, character);
            given(roleService.createRole(filmId, personId, character)).willReturn(expectedRole);

            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .variable(CHARACTER, character)
                    .execute()
                    .path(CREATE_ROLE)
                    .entity(Role.class)
                    .matches(role -> Objects.equals(role.getFilm().getId(), filmId))
                    .matches(role -> Objects.equals(role.getPerson().getId(), personId))
                    .matches(role -> Objects.equals(role.getCharacter(), character));
        }

        @ParameterizedTest(name = "{argumentsWithNames}")
        @MethodSource("com.demo.filmdb.graphql.RoleControllerTests#invalidRoleInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Long filmId, Long personId, String character) {
            graphQlTester
                    .documentName(CREATE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .variable(CHARACTER, character)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(GET_ROLE)
    class GetRole {

        @Test
        @DisplayName("Existing ids, correct response")
        void ExistingId_CorrectResponse() {
            Long filmId = 12L;
            Long personId = 42L;
            Role expectedRole = createRole(filmId, personId);
            given(roleService.getRole(filmId, personId)).willReturn(expectedRole);

            graphQlTester
                    .documentName(GET_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .execute()
                    .path(GET_ROLE)
                    .entity(Role.class)
                    .matches(role -> Objects.equals(role.getCharacter(), expectedRole.getCharacter()))
                    .matches(role -> Objects.equals(role.getFilm().getId(), filmId))
                    .matches(role -> Objects.equals(role.getPerson().getId(), personId));
        }

        @Test
        @DisplayName("Non existing id, null response")
        void NotExistingId_NullResponse() {
            given(roleService.getRole(anyLong(), anyLong())).willReturn(null);

            graphQlTester
                    .documentName(GET_ROLE)
                    .variable(FILM_ID, 1)
                    .variable(PERSON_ID, 2)
                    .execute()
                    .path(GET_ROLE)
                    .valueIsNull();
        }

        @ParameterizedTest(name = "film id: {0}, person id: {1}")
        @MethodSource("com.demo.filmdb.graphql.Util#invalidCrewMemberIdInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object filmId, Object personId) {
            graphQlTester
                    .documentName(GET_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_ROLE)
    class UpdateRole {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long filmId = 1L;
            final Long personId = 4L;
            final String character = "Frodo";
            final Role expectedRole = createRole(filmId, personId, character);
            given(roleService.updateRole(filmId, personId, character)).willReturn(expectedRole);

            graphQlTester
                    .documentName(UPDATE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .variable(CHARACTER, character)
                    .execute()
                    .path(UPDATE_ROLE)
                    .entity(Role.class)
                    .matches(role -> Objects.equals(role.getFilm().getId(), filmId))
                    .matches(role -> Objects.equals(role.getPerson().getId(), personId))
                    .matches(role -> Objects.equals(role.getCharacter(), character));
        }

        @ParameterizedTest(name = "{argumentsWithNames}")
        @MethodSource("com.demo.filmdb.graphql.RoleControllerTests#invalidRoleInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Long filmId, Long personId, String character) {
            graphQlTester
                    .documentName(UPDATE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .variable(CHARACTER, character)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(DELETE_ROLE)
    class DeleteRole {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long filmId = 1L;
            final Long personId = 4L;

            graphQlTester
                    .documentName(DELETE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .execute()
                    .path(DELETE_ROLE)
                    .entity(DeleteRolePayload.class)
                    .matches(payload -> Objects.equals(payload.filmId(), filmId))
                    .matches(payload -> Objects.equals(payload.personId(), personId));
        }

        @ParameterizedTest(name = "{argumentsWithNames}")
        @MethodSource("com.demo.filmdb.graphql.Util#invalidCrewMemberIdInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object filmId, Object personId) {
            graphQlTester
                    .documentName(DELETE_ROLE)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
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
                arguments(null, 1L, character),
                arguments(1L, null, character),
                arguments(1L, 1L, null),
                arguments(1L, 1L, "")
        );
    }
}
