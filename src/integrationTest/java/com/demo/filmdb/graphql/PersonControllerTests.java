package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.payloads.DeletePersonPayload;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.utils.SortUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@GraphQlTest({PersonController.class, TestConfigurer.class})
@DisplayName("GraphQL Person")
public class PersonControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    private PersonService personService;

    @Nested
    @DisplayName(PEOPLE)
    class People {

        @Test
        @DisplayName("No arguments, runs successfully")
        void NotAuthenticated_Authorized() {
            graphQlTester.document("{people {id}}")
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Valid input, correct Service call")
        void ValidInput_CorrectServiceCall() {
            int page = 3;
            int pageSize = 14;
            var sortBy = SortUtil.SortablePersonField.ID;
            var sortDirection = Sort.Direction.ASC;
            var name = "Jessica";
            var bornAfter = LocalDate.of(1950, 11, 11);

            graphQlTester.documentName(PEOPLE)
                    .variable(PAGE, page)
                    .variable(PAGE_SIZE, pageSize)
                    .variable(SORT_BY, sortBy)
                    .variable(SORT_DIRECTION, sortDirection)
                    .variable(NAME, name)
                    .variable("bornAfter", bornAfter)
                    .executeAndVerify();

            verify(personService).getPeople(
                    page,
                    pageSize,
                    sortBy.getFieldName(),
                    sortDirection,
                    name,
                    bornAfter,
                    null
            );
        }

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.PersonControllerTests#invalidPeopleInputs")
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(
                Object page,
                Object pageSize,
                Object sortBy,
                Object sortDirection,
                Object name,
                Object bornAfter,
                Object bornBefore
        ) {
            graphQlTester.documentName(PEOPLE)
                    .variable(PAGE, page)
                    .variable(PAGE_SIZE, pageSize)
                    .variable(SORT_BY, sortBy)
                    .variable(SORT_DIRECTION, sortDirection)
                    .variable(NAME, name)
                    .variable("bornAfter", bornAfter)
                    .variable("bornBefore", bornBefore)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(CREATE_PERSON)
    class CreatePerson {

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.PersonControllerTests#validPersonInputs")
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse(String name, LocalDate dateOfBirth) {
            given(personService.createPerson(any())).willReturn(new Person(name, dateOfBirth));

            graphQlTester
                    .documentName(CREATE_PERSON)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .path(CREATE_PERSON + ".person")
                    .entity(Person.class)
                    .matches(person -> Objects.equals(person.getName(), name))
                    .matches(person -> Objects.equals(person.getDateOfBirth(), dateOfBirth));

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
    @DisplayName(GET_PERSON)
    class GetPerson {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            final Long id = 20L;
            final String name = "Jonah Hill";
            final LocalDate dateOfBirth = LocalDate.of(1983, 12, 20);
            given(personService.getPerson(id)).willReturn(Optional.of(createPerson(id, name, dateOfBirth)));

            graphQlTester
                    .documentName(GET_PERSON)
                    .variable(VAR_ID, id)
                    .execute()
                    .path(GET_PERSON)
                    .entity(Person.class)
                    .matches(person -> Objects.equals(person.getId(), id))
                    .matches(person -> Objects.equals(person.getName(), name))
                    .matches(person -> Objects.equals(person.getDateOfBirth(), dateOfBirth));
        }

        @Test
        @DisplayName("Not existing id, returns null")
        void NotExistingId_ReturnsNull() {
            given(personService.getPerson(anyLong())).willReturn(Optional.empty());

            graphQlTester
                    .documentName(GET_PERSON)
                    .variable(VAR_ID, 1L)
                    .execute()
                    .path(GET_PERSON)
                    .valueIsNull();
        }
    }

    @Nested
    @DisplayName(UPDATE_PERSON)
    class UpdatePerson {

        @ParameterizedTest(name = ARGUMENTS_WITH_NAMES_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.graphql.PersonControllerTests#validPersonInputs")
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse(String name, LocalDate dateOfBirth) {
            final Long id = 1L;
            given(personService.updatePerson(anyLong(), any())).willReturn(createPerson(id, name, dateOfBirth));

            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(VAR_ID, id)
                    .variable(NAME, name)
                    .variable(DATE_OF_BIRTH, dateOfBirth)
                    .execute()
                    .path(UPDATE_PERSON + ".person")
                    .entity(Person.class)
                    .matches(person -> Objects.equals(person.getId(), id))
                    .matches(person -> Objects.equals(person.getName(), name))
                    .matches(person -> Objects.equals(person.getDateOfBirth(), dateOfBirth));

        }

        @Test
        @DisplayName("Not existing id, not found error")
        void NotExistingId_NotFoundError() {
            given(personService.updatePerson(anyLong(), any())).willThrow(new EntityNotFoundException("404"));

            graphQlTester
                    .documentName(UPDATE_PERSON)
                    .variable(VAR_ID, 1L)
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
                    .variable(VAR_ID, 1L)
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
    @DisplayName(DELETE_PERSON)
    class DeletePerson {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            Long expectedId = 1L;

            graphQlTester
                    .documentName(DELETE_PERSON)
                    .variable(VAR_ID, expectedId)
                    .execute()
                    .path(DELETE_PERSON)
                    .entity(DeletePersonPayload.class)
                    .matches(payload -> Objects.equals(payload.id(), expectedId));
        }

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        // Valid: "mutation { deletePerson(input: {id: 1}) { id } }"
        @ValueSource(strings = {
                "mutation { deletePerson(input: {id: 1}) }",
                "mutation { deletePerson(input: null) { id } }",
                "mutation { deletePerson(input: {}) { id } }",
                "mutation { deletePerson(input: {id: null}) { id } }",
                "mutation { deletePerson(input: {id: \"one\"}) { id } }",
        })
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError(String document) {
            graphQlTester
                    .document(document)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    private static Stream<Arguments> validPersonInputs() {
        final String name = "Anne Hathaway";
        final LocalDate dateOfBirth = LocalDate.of(1982, 11, 12);
        return Stream.of(
                arguments(name, dateOfBirth),
                arguments(name, NULL)
        );
    }

    private static Stream<Arguments> invalidPersonInputs() {
        final String name = "Matthew McConaughey";
        final LocalDate dateOfBirth = LocalDate.of(1969, 11, 4);
        return Stream.of(
                arguments(name, INVALID_DATE),
                arguments(NULL, dateOfBirth),
                arguments(EMPTY_STRING, dateOfBirth),
                arguments(BLANK_STRING, dateOfBirth)
        );
    }

    private static Stream<Arguments> invalidPeopleInputs() {
        int page = 3;
        int pageSize = 14;
        var sortBy = SortUtil.SortablePersonField.NAME;
        var sortDirection = Sort.Direction.ASC;
        var name = "weave";
        var bornAfter = LocalDate.of(1950, 10, 10);
        var bornBefore = LocalDate.of(1990, 11, 11);
        return Stream.of(
                arguments(NULL, pageSize, sortBy, sortDirection, name, bornAfter, bornBefore),
                arguments(page, NULL, sortBy, sortDirection, name, bornAfter, bornBefore),
                arguments(page, pageSize, sortBy, sortDirection, BLANK_STRING, bornAfter, bornBefore),
                arguments(page, pageSize, sortBy, sortDirection, name, INVALID_DATE, bornBefore),
                arguments(page, pageSize, sortBy, sortDirection, name, bornAfter, INVALID_DATE)
        );
    }
}
