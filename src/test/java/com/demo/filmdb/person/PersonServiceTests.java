package com.demo.filmdb.person;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.person.specifications.PersonWithName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersonServiceTests extends ServiceTest {

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService(personRepository, roleRepository);
    }

    @Test
    void search_ValidArguments_Finds() {
        Specification<Person> expectedSpec = Specification.where(new PersonWithName("Joe"));
        Pageable expectedPageable = PageRequest.of(1, 5);

        personService.search(expectedSpec, expectedPageable);

        verify(personRepository).findAll(expectedSpec, expectedPageable);
    }

    @Test
    void getAllPeople_Finds() {
        final Pageable expectedPageable = Pageable.unpaged();

        personService.getAllPeople(expectedPageable);

        verify(personRepository).findAll(expectedPageable);
    }

    @Nested
    @DisplayName("createPerson")
    class CreatePerson {

        @Test
        @DisplayName("Valid input, saves and returns")
        void ValidInput_Creates() {
            var name = "Bruce Willis";
            var dateOfBirth = LocalDate.of(1955, 3, 19);
            PersonInfo input = createPersonInfo(name, dateOfBirth);
            when(personRepository.save(any(Person.class))).then(AdditionalAnswers.returnsFirstArg());

            Person actual = personService.createPerson(input);

            // assert saved
            var savedPerson = ArgumentCaptor.forClass(Person.class);
            verify(personRepository).save(savedPerson.capture());
            assertThat(savedPerson.getValue().getName()).isEqualTo(name);
            assertThat(savedPerson.getValue().getDob()).isEqualTo(dateOfBirth);
            // assert returned
            assertThat(actual.getName()).isEqualTo(name);
            assertThat(actual.getDob()).isEqualTo(dateOfBirth);
        }
    }

    @Test
    void savePerson_ValidPerson_Saves() {
        Person expectedPerson = new Person("Ivan Ivanoff", LocalDate.of(1977, 7, 7));

        personService.savePerson(expectedPerson);

        verify(personRepository).save(expectedPerson);
    }

    @Nested
    class GetPerson {
        @Test
        @DisplayName("Finds existing person by id")
        void ExistingId_Finds() {
            final Long expectedPersonId = 11L;
            given(personRepository.findById(expectedPersonId)).willReturn(Optional.of(createPerson(expectedPersonId)));

            var actual = personService.getPerson(expectedPersonId);

            verify(personRepository).findById(expectedPersonId);
            assert actual.isPresent();
            assertThat(actual.get().getId()).isEqualTo(expectedPersonId);
        }

        @Test
        @DisplayName("Given not existing id returns null")
        void NotExistingId_ReturnsNull() {
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            var actual = personService.getPerson(11L);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class DeletePerson {
        @Test
        @DisplayName("Deletes existing person")
        void ExistingId_Deletes() {
            final Long expectedPersonId = 11L;
            Person expected = new Person();
            expected.setId(expectedPersonId);

            personService.deletePerson(expected);

            verify(personRepository).delete(expected);
            verify(roleRepository).deleteById_PersonId(expectedPersonId);
        }
    }

    @Nested
    class GetPeople {
        @Test
        @DisplayName("Given existing ids, finds")
        public void ExistingIds_Finds() {
            final List<Long> expectedPeopleIds = List.of(1L, 2L, 3L);
            given(personRepository.findAllById(anyCollection()))
                    .willReturn(createPeoplesWithIds(expectedPeopleIds));

            personService.getPeople(expectedPeopleIds);

            verify(personRepository).findAllById(expectedPeopleIds);
        }

        @ParameterizedTest(name = "given {0}, exist {1}")
        @MethodSource("com.demo.filmdb.person.PersonServiceTests#expectedAndActualIdsProvider")
        @DisplayName("Given not existing ids, returns only people for existing ids")
        public void NotExistingIds_FindsExisting(List<Long> givenIds, List<Long> existingIds) {
            given(personRepository.findAllById(anyCollection()))
                    .willReturn(createPeoplesWithIds(existingIds));

            List<Person> actual = personService.getPeople(givenIds);

            Set<Long> expectedIds = new HashSet<>(existingIds);
            Set<Long> actualIds = actual.stream().map((Person::getId)).collect(Collectors.toSet());
            assertThat(actualIds).isEqualTo(expectedIds);
        }
    }

    /* Utility */

    private List<Person> createPeoplesWithIds(List<Long> ids) {
        return ids.stream().map(id -> {
            Person person = new Person();
            person.setId(id);
            return person;
        }).toList();
    }

    private static Stream<Arguments> expectedAndActualIdsProvider() {
        return Stream.of(
                Arguments.arguments(List.of(1L, 2L, 3L), List.of(1L, 2L)),
                Arguments.arguments(List.of(1L, 2L, 3L), List.of(3L)),
                Arguments.arguments(List.of(1L, 2L, 3L), List.of())
        );
    }

    private PersonInfo createPersonInfo(String name, LocalDate dateOfBirth) {
        return new PersonInfo() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public LocalDate getDateOfBirth() {
                return dateOfBirth;
            }
        };
    }
}
