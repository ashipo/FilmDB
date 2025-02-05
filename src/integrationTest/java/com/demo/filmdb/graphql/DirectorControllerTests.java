package com.demo.filmdb.graphql;

import com.demo.filmdb.director.DirectorService;
import com.demo.filmdb.graphql.payloads.SetDirectorPayload;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.CollectionAssert.assertThatCollection;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@GraphQlTest({DirectorController.class, TestConfigurer.class})
@DisplayName("GraphQL Director")
public class DirectorControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    @SuppressWarnings("unused")
    private DirectorService directorService;

    @Nested
    @DisplayName(SET_DIRECTOR)
    class SetDirector {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long filmId = 1L;
            final Long personId = 2L;

            graphQlTester
                    .documentName(SET_DIRECTOR)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .execute()
                    .path(SET_DIRECTOR)
                    .entity(SetDirectorPayload.class)
                    .matches(payload -> Objects.equals(payload.filmId(), filmId))
                    .matches(payload -> Objects.equals(payload.personId(), personId));
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.Util#invalidCrewMemberIdInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object filmId, Object personId) {
            graphQlTester
                    .documentName(SET_DIRECTOR)
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
    @DisplayName(DELETE_DIRECTOR)
    class DeleteDirector {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long filmId = 1L;
            final Long personId = 2L;

            graphQlTester
                    .documentName(DELETE_DIRECTOR)
                    .variable(FILM_ID, filmId)
                    .variable(PERSON_ID, personId)
                    .execute()
                    .path(DELETE_DIRECTOR)
                    .entity(SetDirectorPayload.class)
                    .matches(payload -> Objects.equals(payload.filmId(), filmId))
                    .matches(payload -> Objects.equals(payload.personId(), personId));
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.Util#invalidCrewMemberIdInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object filmId, Object personId) {
            graphQlTester
                    .documentName(DELETE_DIRECTOR)
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
    @DisplayName(UPDATE_DIRECTORS)
    class UpdateDirectors {

        @Captor
        ArgumentCaptor<List<Long>> idsCaptor;

        @Test
        @DisplayName("Valid input, updates correctly")
        void ValidInput_CorrectResponse() {
            Long filmId = 5L;
            List<Long> directorsIds = List.of(3L, 7L);

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable(FILM_ID, filmId)
                    .variable(DIRECTORS_IDS, directorsIds)
                    .executeAndVerify();

            ArgumentCaptor<Long> filmIdCaptor = ArgumentCaptor.forClass(Long.class);
            verify(directorService).updateDirectors(filmIdCaptor.capture(), idsCaptor.capture());
            assertThat(filmIdCaptor.getValue()).isEqualTo(filmId);
            assertThatCollection(idsCaptor.getValue()).containsExactlyElementsOf(directorsIds);
        }

        @Test
        @DisplayName("Not existing ids, not found error")
        void NotExistingIds_NotFoundError() {
            given(directorService.updateDirectors(anyLong(), anyCollection())).willThrow(new EntityNotFoundException("Msg"));

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable(FILM_ID, 5L)
                    .variable(DIRECTORS_IDS, List.of(3L))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(UPDATE_DIRECTORS)
                    .valueIsNull();
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.DirectorControllerTests#invalidUpdateDirectorsInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object filmId, Object directorsIds) {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable(FILM_ID, filmId)
                    .variable(DIRECTORS_IDS, directorsIds)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    private static Stream<Arguments> invalidUpdateDirectorsInputs() {
        final List<Long> directors = List.of(1L, 2L);
        return Stream.of(
                arguments(NULL, directors),
                arguments("one", directors),
                arguments(1L, List.of("one", "two"))
        );
    }
}
