package com.demo.filmdb.role;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class RoleServiceTests extends ServiceTest {

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    @Nested
    class GetRole {
        @Test
        @DisplayName("Given existing ids, finds")
        void ExistingIds_Finds(){
            final long expectedFilmId = 11L;
            final long expectedPersonId = 11L;
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.of(new Role()));

            roleService.getRole(expectedFilmId, expectedPersonId);

            verify(roleRepository).findByIds(expectedFilmId, expectedPersonId);
        }

        @Test
        @DisplayName("Given at least one not existing id, throws 404")
        void NotExistingIds_Throws404(){
            final long expectedFilmId = 11L;
            final long expectedPersonId = 11L;
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> roleService.getRole(expectedFilmId, expectedPersonId));

            assertThatValid404(thrown, expectedFilmId, expectedPersonId);
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
    class DeleteRole {
        @Test
        @DisplayName("Given existing Ids, deletes")
        void ExistingIds_Deletes() {
            long expectedFilmId = 11L;
            long expectedPersonId = 11L;
            Role expectedRole = new Role();
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.of(expectedRole));

            roleService.deleteRole(expectedFilmId, expectedPersonId);

            verify(roleRepository).delete(expectedRole);
        }

        @Test
        @DisplayName("Given at least one not existing Id, throws 404")
        void NotExistingIds_Throws404() {
            long expectedFilmId = 11L;
            long expectedPersonId = 11L;
            given(roleRepository.findByIds(expectedFilmId, expectedPersonId)).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> roleService.deleteRole(expectedFilmId, expectedPersonId));

            assertThatValid404(thrown, expectedFilmId, expectedPersonId);        }
    }

    @Nested
    class CreateRole {
        @Test
        @DisplayName("Given valid data, saves")
        void NotExistingRole_Saves() {
            long expectedFilmId = 2L;
            long expectedPersonId = 3L;
            String expectedCharacter = "butler";
            given(roleRepository.existsById(any())).willReturn(false);

            roleService.createRole(createFilm(expectedFilmId), createPerson(expectedPersonId), expectedCharacter);

            ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(roleCaptor.capture());
            Role actualRole = roleCaptor.getValue();
            assertThat(actualRole.getFilm().getId()).as("Film id").isEqualTo(expectedFilmId);
            assertThat(actualRole.getPerson().getId()).as("Person id").isEqualTo(expectedPersonId);
            assertThat(actualRole.getCharacter()).as("Character").isEqualTo(expectedCharacter);
        }

        @Test
        @DisplayName("Given already existing role, throws 409")
        void ExistingRole_Throws409() {
            long expectedFilmId = 2L;
            long expectedPersonId = 3L;
            given(roleRepository.existsById(any())).willReturn(true);

            Throwable thrown = catchThrowable(() -> roleService.
                    createRole(createFilm(expectedFilmId), createPerson(expectedPersonId), "butler"));

            assertThat(thrown).as("Expected ResponseStatusException to be thrown")
                    .isInstanceOf(ResponseStatusException.class);
            ResponseStatusException statusException = (ResponseStatusException) thrown;
            assertThat(statusException.getStatusCode().value()).as("Status code").isEqualTo(409);
            assertThat(thrown).hasMessageContaining("Role for film %d and person %d already exists.",
                    expectedFilmId, expectedPersonId);
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
            assertThat(actualRole).isPresent();
            assertThat(actualRole.get().getCharacter()).isEqualTo(expectedCharacter);
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

    private void assertThatValid404(Throwable thrown, long filmId, long personId){
        assertThatValid404(thrown);
        assertThat(thrown).hasMessageContaining("Could not find role with filmId %d and personId %d",
                filmId, personId);
    }

    private Film getFilmWithRole(long filmId, long actorId) {
        Film film = createFilm(filmId);
        Person person = createPerson(actorId);
        Role role = new Role(film, person, "Narrator");
        role.setId(new RoleKey(filmId, actorId));
        film.setRoles(Set.of(role));
        return film;
    }
}
