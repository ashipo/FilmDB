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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @Test
    void savePerson_ValidPerson_Saves() {
        Person expectedPerson = new Person("Ivan Ivanoff", LocalDate.of(1977, 7, 7));

        personService.savePerson(expectedPerson);

        verify(personRepository).save(expectedPerson);
    }

    @Nested
    class GetPerson {
        @Test
        @DisplayName("(existing id) finds")
        void ExistingId_Finds() {
            final long expectedPersonId = 11L;
            given(personRepository.findById(anyLong())).willReturn(Optional.of(new Person()));

            personService.getPerson(expectedPersonId);

            verify(personRepository).findById(expectedPersonId);
        }

        @Test
        @DisplayName("(not existing id) throws 404")
        void NotExistingId_Throws404() {
            final long expectedPersonId = 11L;
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> personService.getPerson(expectedPersonId));

            assertThatValid404(thrown, expectedPersonId);
        }
    }

    @Nested
    class DeletePerson {
        @Test
        @DisplayName("(existing id) deletes")
        void ExistingId_Deletes() {
            final long expectedPersonId = 11L;
            given(personRepository.findById(anyLong())).willReturn(Optional.of(new Person()));

            personService.deletePerson(expectedPersonId);

            verify(personRepository).deleteById(expectedPersonId);
            verify(roleRepository).deleteById_PersonId(expectedPersonId);
        }

        @Test
        @DisplayName("(not existing id) throws 404")
        void NotExistingId_Throws404() {
            final long expectedPersonId = 11L;
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> personService.deletePerson(expectedPersonId));

            assertThatValid404(thrown, expectedPersonId);
            verify(roleRepository, never()).deleteById_PersonId(anyLong());
            verify(personRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    class GetRoles {
        @Test
        public void ExistingId_Finds() {
            final long expectedPersonId = 3L;
            given(personRepository.findById(anyLong())).willReturn(Optional.of(new Person()));

            personService.getRoles(expectedPersonId);

            verify(personRepository).findById(expectedPersonId);
        }

        @Test
        public void NotExistingId_Throws404() {
            final long expectedPersonId = 3L;
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> personService.getRoles(expectedPersonId));

            assertThatValid404(thrown, expectedPersonId);
        }
    }

    @Nested
    class GetDirected {
        @Test
        public void ExistingId_Finds() {
            final long expectedPersonId = 3L;
            given(personRepository.findById(anyLong())).willReturn(Optional.of(new Person()));

            personService.getDirected(expectedPersonId);

            verify(personRepository).findById(expectedPersonId);
        }

        @Test
        public void NotExistingId_Throws404() {
            final long expectedPersonId = 3L;
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> personService.getDirected(expectedPersonId));

            assertThatValid404(thrown, expectedPersonId);
        }
    }

    @Nested
    class GetPeople{
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
        @DisplayName("Given not existing ids, throws 404")
        public void NotExistingIds_Throws404(List<Long> givenIds, List<Long> existingIds) {
            given(personRepository.findAllById(anyCollection()))
                    .willReturn(createPeoplesWithIds(existingIds));
            List<Long> expectedNotFoundIds = new ArrayList<>(givenIds);
            expectedNotFoundIds.removeAll(existingIds);

            Throwable thrown = catchThrowable(() -> personService.getPeople(givenIds));

            assertThatValid404(thrown, expectedNotFoundIds);
        }
    }

    /* Utility */

    private void assertThatValid404(Throwable thrown, long notFoundId){
        assertThatValid404(thrown);
        assertThat(thrown).hasMessageContaining("Could not find person with id %d", notFoundId);
    }

    private void assertThatValid404(Throwable thrown, List<Long> notFoundIds){
        assertThatValid404(thrown);
        assertThat(thrown).hasMessageContaining("Could not find people with ids " + notFoundIds);
    }

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
}
