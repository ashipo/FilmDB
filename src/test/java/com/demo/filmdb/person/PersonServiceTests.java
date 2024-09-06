package com.demo.filmdb.person;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class PersonServiceTests extends ServiceTest {

    private PersonService personService;
    private final PersonMapper personMapper = Mappers.getMapper(PersonMapper.class);
    @Mock
    private PersonSpecs personSpecs;

    @BeforeEach
    void setUp() {
        personService = new PersonService(personRepository, roleRepository, personMapper, personSpecs);
    }

    @Nested
    @DisplayName("getPeople")
    class GetPeople {

        @Nested
        @DisplayName("with pageable parameter")
        class WithPageable {

            @Test
            @DisplayName("Valid arguments, searches correctly")
            void ValidArguments_SearchesCorrectly() {
                int pageNumber = 13;
                int pageSize = 37;
                var direction = Sort.Direction.DESC;
                String sortBy = "id";
                var pageable = PageRequest.of(pageNumber, pageSize, direction, sortBy);

                personService.getPeople(pageable);

                var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(personRepository).findAll(pageableCaptor.capture());
                var actualPageable = pageableCaptor.getValue();
                assertThat(actualPageable.getPageNumber()).isEqualTo(pageNumber);
                assertThat(actualPageable.getPageSize()).isEqualTo(pageSize);
                var actualSort = actualPageable.getSort().getOrderFor(sortBy);
                assertThat(actualSort).isNotNull();
                assert actualSort != null;
                assertThat(actualSort.getDirection()).isEqualTo(direction);
            }
        }

        @Nested
        @DisplayName("with pageable and filter parameters")
        class WithPageableAndFilter {

            @Test
            @DisplayName("Valid arguments, searches correctly")
            void ValidArguments_SearchesCorrectly() {
                int pageNumber = 13;
                int pageSize = 37;
                var direction = Sort.Direction.DESC;
                String sortBy = "name";
                var pageable = PageRequest.of(pageNumber, pageSize, direction, sortBy);
                String name = "Joe";
                LocalDate bornBefore = LocalDate.of(2222, 2, 2);
                given(personSpecs.nameContains(name)).willReturn(emptySpec());
                given(personSpecs.bornAfter(null)).willReturn(emptySpec());
                given(personSpecs.bornBefore(bornBefore)).willReturn(emptySpec());

                personService.getPeople(pageable, name, null, bornBefore);

                var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(personRepository).findAll(ArgumentMatchers.<Specification<Person>>any(), pageableCaptor.capture());
                // verify paging
                var actualPageable = pageableCaptor.getValue();
                assertThat(actualPageable.getPageNumber()).isEqualTo(pageNumber);
                assertThat(actualPageable.getPageSize()).isEqualTo(pageSize);
                // verify sorting
                var actualSort = actualPageable.getSort().getOrderFor(sortBy);
                assertThat(actualSort).isNotNull();
                assert actualSort != null;
                assertThat(actualSort.getDirection()).isEqualTo(direction);
                // verify filtering
                verify(personSpecs).nameContains(name);
                verify(personSpecs).bornAfter(null);
                verify(personSpecs).bornBefore(bornBefore);
            }
        }
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
            assertThat(savedPerson.getValue().getDateOfBirth()).isEqualTo(dateOfBirth);
            // assert returned
            assertThat(actual.getName()).isEqualTo(name);
            assertThat(actual.getDateOfBirth()).isEqualTo(dateOfBirth);
        }
    }

    @Nested
    @DisplayName("updatePerson")
    class UpdatePerson {

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        @MethodSource("com.demo.filmdb.person.PersonServiceTests#updatePersonProvider")
        @DisplayName("Existing id, updates")
        void ExistingId_Updates(String expectedName, LocalDate expectedDateOfBirth) {
            final Long personId = 5L;
            final Person existingPerson = createPerson(personId, "Leeloo", LocalDate.of(1, 1, 1));
            // find existing person
            given(personRepository.findById(personId)).willReturn(Optional.of(existingPerson));
            // return updated person
            when(personRepository.save(any(Person.class))).then(AdditionalAnswers.returnsFirstArg());

            Person actual = personService.updatePerson(personId, createPersonInfo(expectedName, expectedDateOfBirth));

            // assert saved
            var updatedPersonCaptor = ArgumentCaptor.forClass(Person.class);
            verify(personRepository).save(updatedPersonCaptor.capture());
            Person updatedPerson = updatedPersonCaptor.getValue();
            assertThat(updatedPerson.getId()).isEqualTo(personId);
            assertThat(updatedPerson.getName()).isEqualTo(expectedName);
            assertThat(updatedPerson.getDateOfBirth()).isEqualTo(expectedDateOfBirth);
            // assert returned
            assertThat(actual.getId()).isEqualTo(personId);
            assertThat(actual.getName()).isEqualTo(expectedName);
            assertThat(actual.getDateOfBirth()).isEqualTo(expectedDateOfBirth);
        }

        @Test
        @DisplayName("Not existing id, throws EntityNotFoundException")
        void NotExistingId_Throws() {
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    personService.updatePerson(1L, createPersonInfo("Gary Oldman", LocalDate.of(1958, 3, 21)))
            );
        }
    }

    @Nested
    @DisplayName("getPerson")
    class GetPerson {

        @Test
        @DisplayName("Existing id, finds and returns")
        void ExistingId_Finds() {
            final Long expectedPersonId = 11L;
            given(personRepository.findById(expectedPersonId)).willReturn(Optional.of(createPerson(expectedPersonId)));

            var actual = personService.getPerson(expectedPersonId);

            verify(personRepository).findById(expectedPersonId);
            assert actual.isPresent();
            assertThat(actual.get().getId()).isEqualTo(expectedPersonId);
        }

        @Test
        @DisplayName("Not existing id, returns empty Optional")
        void NotExistingId_ReturnsNull() {
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            var actual = personService.getPerson(11L);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletePerson")
    class DeletePerson {

        @Test
        @DisplayName("Existing Id, deletes correctly")
        void ExistingId_DeletesCorrectly() {
            final Long id = 11L;
            Person person = mock(Person.class);
            given(personRepository.findById(id)).willReturn(Optional.of(person));

            personService.deletePerson(id);

            // assert director associations are deleted
            verify(person).removeFilmsDirected();
            // assert roles are deleted
            verify(roleRepository).deleteById_PersonId(id);
            verify(personRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("personExists")
    class PersonExists {

        @Test
        @DisplayName("Existing person id, returns true")
        void ExistingId_ReturnsTrue() {
            final Long id = 1L;
            given(personRepository.existsById(id)).willReturn(true);

            boolean actual = personService.personExists(id);

            assertThat(actual).isTrue();
        }

        @Test
        @DisplayName("Not existing person id, returns false")
        void NotExistingId_ReturnsFalse() {
            final Long id = 1L;
            given(personRepository.existsById(id)).willReturn(false);

            boolean actual = personService.personExists(id);

            assertThat(actual).isFalse();
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

    private static Stream<Arguments> updatePersonProvider() {
        final String name = "Milla Jovovich";
        return Stream.of(
                Arguments.arguments(name, LocalDate.of(1975, 12, 17)),
                Arguments.arguments(name, null)
        );
    }

    private PersonInfo createPersonInfo(String name, @Nullable LocalDate dateOfBirth) {
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
