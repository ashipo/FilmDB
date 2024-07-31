package com.demo.filmdb.role;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleServiceTests extends ServiceTest {

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
        roleService.setServices(personService, filmService);
    }

    @Nested
    @DisplayName("getRole")
    class GetRole {

        @Test
        @DisplayName("Given existing ids, finds")
        void ExistingIds_Finds() {
            final Long expectedFilmId = 11L;
            final Long expectedPersonId = 11L;
            final Role expectedRole = new Role();
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.of(expectedRole));

            Role actual = roleService.getRole(expectedFilmId, expectedPersonId);

            verify(roleRepository).findByIds(expectedFilmId, expectedPersonId);
            assertThat(actual).isEqualTo(expectedRole);
        }

        @Test
        @DisplayName("Given ids of a non existing role, returns null")
        void NotExistingIds_ReturnsNull() {
            given(roleRepository.findByIds(anyLong(), anyLong())).willReturn(Optional.empty());

            Role actual = roleService.getRole(11L, 11L);

            assertThat(actual).isNull();
        }
    }

    @Test
    @DisplayName("Given valid Role, saves")
    void saveRole_ValidRole_Saves() {
        Role expectedRole = new Role();

        roleService.saveRole(expectedRole);

        verify(roleRepository).save(expectedRole);
    }

    @Nested
    @DisplayName("deleteRole")
    class DeleteRole {

        @Test
        @DisplayName("Deletes correctly")
        void ValidIds_DeletesCorrectly() {
            final Long filmId = 1L;
            final Long personId = 2L;
            final RoleKey expectedId = new RoleKey(filmId, personId);

            roleService.deleteRole(filmId, personId);

            verify(roleRepository).deleteById(expectedId);
        }
    }

    @Nested
    @DisplayName("createRole")
    class CreateRole {

        @Test
        @DisplayName("Valid role, saves")
        void ValidRole_Saves() {
            Long expectedFilmId = 2L;
            Long expectedPersonId = 3L;
            String expectedCharacter = "butler";
            given(filmService.getFilm(anyLong())).willReturn(createFilm(expectedFilmId));
            given(personService.getPerson(anyLong())).willReturn(createPerson(expectedPersonId));
            given(roleRepository.findById(any())).willReturn(Optional.empty());
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            Role actual = roleService.createRole(expectedFilmId, expectedPersonId, expectedCharacter);

            assertThat(actual.getFilm().getId()).as("Film id").isEqualTo(expectedFilmId);
            assertThat(actual.getPerson().getId()).as("Person id").isEqualTo(expectedPersonId);
            assertThat(actual.getCharacter()).as("Character").isEqualTo(expectedCharacter);
        }

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        void NotExistingFilmId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(null);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.createRole(1L, 1L, "Char")
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(new Film());
            given(personService.getPerson(anyLong())).willReturn(null);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.createRole(1L, 1L, "Char")
            );
        }

        @Test
        @DisplayName("Existing role, throws EntityAlreadyExistsException")
        void ExistingRole_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(new Film());
            given(personService.getPerson(anyLong())).willReturn(new Person());
            given(roleRepository.findById(any())).willReturn(Optional.of(new Role()));

            assertThatExceptionOfType(EntityAlreadyExistsException.class).isThrownBy(() ->
                    roleService.createRole(1L, 1L, "Char")
            );
        }
    }

    @Nested
    @DisplayName("updateRole")
    class UpdateRole {

        @Test
        @DisplayName("Valid role, updates")
        void ValidRole_Updates() {
            Long expectedFilmId = 2L;
            Long expectedPersonId = 3L;
            String expectedCharacter = "Updated character";
            Role originalRole = createRole(expectedFilmId, expectedPersonId, "Original character");
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.of(originalRole));
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            Role actual = roleService.updateRole(expectedFilmId, expectedPersonId, expectedCharacter);

            assertThat(actual.getFilm().getId()).as("Film id").isEqualTo(expectedFilmId);
            assertThat(actual.getPerson().getId()).as("Person id").isEqualTo(expectedPersonId);
            assertThat(actual.getCharacter()).as("Character").isEqualTo(expectedCharacter);
        }

        @Test
        @DisplayName("Not existing role, throws EntityNotFoundException")
        void NotExistingRole_Throws() {
            given(roleRepository.findByIds(anyLong(), anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateRole(1L, 1L, "Lawrence")
            );
        }
    }

    @Nested
    class UpdateCast {
        @Test
        @DisplayName("Given a cast that doesn't contain an existing role, deletes that role")
        void RoleToDelete_Deletes() {
            long expectedFilmId = 2;
            long expectedPersonId = 1;
            long newPersonId = expectedPersonId + 1;    //new actor's id, must be different from the old one
            Film film = getFilmWithRole(expectedFilmId, expectedPersonId);
            Map<Person, String> newCast = Map.of(createPerson(newPersonId), "Husk");

            roleService.updateCast(film, newCast);

            ArgumentCaptor<Role> deletedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).delete(deletedRole.capture());
            assertThat(deletedRole.getValue().getFilm().getId()).isEqualTo(expectedFilmId);
            assertThat(deletedRole.getValue().getPerson().getId()).isEqualTo(expectedPersonId);
        }

        @Test
        @DisplayName("Given a cast that contains an existing role, updates the character for that role")
        void RoleToUpdate_Updates() {
            long expectedFilmId = 2;
            long expectedPersonId = 1;
            Film film = getFilmWithRole(expectedFilmId, expectedPersonId);
            String expectedCharacter = "Thanos";
            Map<Person, String> newCast = Map.of(createPerson(expectedPersonId), expectedCharacter);
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId))
                    .willReturn(film.getRoles().stream().findFirst());

            Set<Role> actualRoles = roleService.updateCast(film, newCast);

            Optional<Role> actualRole = actualRoles.stream().filter(r -> r.getPerson().getId() == expectedPersonId)
                    .findFirst();
            assertThat(actualRole).hasValueSatisfying(role ->
                    assertThat(role.getCharacter()).isEqualTo(expectedCharacter)
            );
        }

        @Test
        @DisplayName("Given a cast with a new role, creates that role")
        void RoleToCreate_Creates() {
            long expectedFilmId = 2;
            long expectedPersonId = 3;
            Film film = getFilmWithRole(expectedFilmId, expectedPersonId + 1);
            String expectedCharacter = "Thanos";
            Map<Person, String> newCast = Map.of(createPerson(expectedPersonId), expectedCharacter);

            roleService.updateCast(film, newCast);

            ArgumentCaptor<Role> savedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(savedRole.capture());
            assertThat(savedRole.getValue().getFilm().getId()).isEqualTo(expectedFilmId);
            assertThat(savedRole.getValue().getPerson().getId()).isEqualTo(expectedPersonId);
            assertThat(savedRole.getValue().getCharacter()).isEqualTo(expectedCharacter);
        }
    }

    /* Utility */

    private Film getFilmWithRole(long filmId, long actorId) {
        Film film = createFilm(filmId);
        Person person = createPerson(actorId);
        Role role = new Role(film, person, "Narrator");
        role.setId(new RoleKey(filmId, actorId));
        film.setRoles(Set.of(role));
        return film;
    }
}
