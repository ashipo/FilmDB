package com.demo.filmdb.graphql;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.util.EntityNotFoundException;
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

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@GraphQlTest({PersonController.class, TestConfigurer.class})
@DisplayName("GraphQL Person")
public class PersonControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    private PersonService personService;

    @Nested
    @DisplayName(CREATE_PERSON)
    class CreatePerson {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final String name = "Anne Hathaway";
            final LocalDate dateOfBirth = LocalDate.of(1982, 11, 12);
            given(personService.createPerson(any())).willReturn(new Person(name, dateOfBirth));

            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .path(CREATE_PERSON + ".person")
                    .entity(Person.class)
                    .matches(person -> Objects.equals(person.getName(), name))
                    .matches(person -> Objects.equals(person.getDob(), dateOfBirth));

        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.PersonControllerTests#invalidPersonInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object name, Object dateOfBirth) {
            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_PERSON)
    class UpdatePerson {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            final Long personId = 1L;
            final String name = "Margot Robbie";
            final LocalDate dateOfBirth = LocalDate.of(1961, 6, 9);
            given(personService.updatePerson(anyLong(), any())).willReturn(createPerson(personId, name, dateOfBirth));

            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(PERSON_ID, personId)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .path(UPDATE_PERSON + ".person")
                    .entity(Person.class)
                    .matches(person -> Objects.equals(person.getId(), personId))
                    .matches(person -> Objects.equals(person.getName(), name))
                    .matches(person -> Objects.equals(person.getDob(), dateOfBirth));

        }

        @Test
        @DisplayName("Not existing id, not found error")
        void NotExistingId_NotFoundError() {
            given(personService.updatePerson(anyLong(), any())).willThrow(new EntityNotFoundException("404"));

            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(PERSON_ID, 1L)
                    .variable(NAME, "Leonardo DiCaprio")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(UPDATE_PERSON)
                    .valueIsNull();

        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.PersonControllerTests#invalidPersonInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(Object name, Object dateOfBirth) {
            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(PERSON_ID, 1L)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    private static Stream<Arguments> invalidPersonInputs() {
        final String name = "Matthew McConaughey";
        final LocalDate dateOfBirth = LocalDate.of(1969, 11, 4);
        return Stream.of(
                arguments(name, "Not a LocalDate"),
                arguments("", dateOfBirth),
                arguments("   ", dateOfBirth)
        );
    }
}
