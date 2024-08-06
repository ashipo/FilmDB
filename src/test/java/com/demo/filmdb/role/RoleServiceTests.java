package com.demo.filmdb.role;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.dtos.CastMember;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RoleService")
class RoleServiceTests extends ServiceTest {

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
            final Long expectedFilmId = 1L;
            final Long expectedPersonId = 2L;
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
    @DisplayName("updateCast")
    class UpdateCast {

        @Test
        @DisplayName("Given a cast with a new role, creates that role")
        void NewRole_Creates() {
            final Long filmId = 15L;
            final Long personId = 8L;
            final String character = "Batman";
            // film without cast
            given(filmService.getFilm(filmId)).willReturn(createFilm(filmId));
            given(personService.getPerson(personId)).willReturn(createPerson(personId));
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            List<Role> actual = roleService.updateCast(filmId, List.of(createCastMember(personId, character)));

            // assert that saved
            ArgumentCaptor<Role> savedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(savedRole.capture());
            assertThat(savedRole.getValue().getFilm().getId()).isEqualTo(filmId);
            assertThat(savedRole.getValue().getPerson().getId()).isEqualTo(personId);
            assertThat(savedRole.getValue().getCharacter()).isEqualTo(character);
            // assert that returned
            assertThatCollection(actual).anyMatch(role -> role.getPerson().getId().equals(personId)
                    && role.getFilm().getId().equals(filmId)
                    && role.getCharacter().equals(character)
            );
        }

        @Test
        @DisplayName("Given a cast with an existing  role, updates that role")
        void ExistingRole_Updates() {
            final Long filmId = 15L;
            final Long personId = 8L;
            final String character = "Batman";
            Film film = createFilm(filmId);
            Role oldRole = createRole(filmId, personId, "Joker");
            film.setCast(Set.of(oldRole));
            given(filmService.getFilm(filmId)).willReturn(film);
            given(roleRepository.findByIds(filmId, personId)).willReturn(Optional.of(oldRole));
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            List<Role> actual = roleService.updateCast(filmId, List.of(createCastMember(personId, character)));

            // assert that saved
            ArgumentCaptor<Role> savedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(savedRole.capture());
            assertThat(savedRole.getValue().getFilm().getId()).isEqualTo(filmId);
            assertThat(savedRole.getValue().getPerson().getId()).isEqualTo(personId);
            assertThat(savedRole.getValue().getCharacter()).isEqualTo(character);
            // assert that returned
            assertThatCollection(actual).anyMatch(role -> role.getPerson().getId().equals(personId)
                    && role.getFilm().getId().equals(filmId)
                    && role.getCharacter().equals(character)
            );
        }

        @Test
        @DisplayName("Given a cast that doesn't contain an existing role, deletes that role")
        void OutdatedRole_Deletes() {
            final Long filmId = 15L;
            final Long personId = 8L;
            Film film = getFilmWithRole(filmId, personId);
            given(filmService.getFilm(filmId)).willReturn(film);

            List<Role> actual = roleService.updateCast(filmId, Collections.emptyList());

            // assert that deleted
            ArgumentCaptor<Role> deletedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).delete(deletedRole.capture());
            assertThat(deletedRole.getValue().getFilm().getId()).isEqualTo(filmId);
            assertThat(deletedRole.getValue().getPerson().getId()).isEqualTo(personId);
            // assert that not returned
            assertThatCollection(actual).noneMatch(role -> role.getPerson().getId().equals(personId)
                    && role.getFilm().getId().equals(filmId)
            );
        }

        @Test
        @DisplayName("Null cast, deletes existing roles")
        void NullCast_Deletes() {
            final Long filmId = 15L;
            final Long personId = 8L;
            Film film = getFilmWithRole(filmId, personId);
            given(filmService.getFilm(filmId)).willReturn(film);

            List<Role> actual = roleService.updateCast(filmId, null);

            // assert that deleted
            ArgumentCaptor<Role> deletedRole = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).delete(deletedRole.capture());
            assertThat(deletedRole.getValue().getFilm().getId()).isEqualTo(filmId);
            assertThat(deletedRole.getValue().getPerson().getId()).isEqualTo(personId);
            // assert that empty collection is returned
            assertThatCollection(actual).isEmpty();
        }

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        void NotExistingFilmId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(null);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateCast(1L, createCast(1L))
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(new Film());
            given(personService.getPerson(anyLong())).willReturn(null);

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateCast(1L, createCast(1L))
            );
        }
    }

    /* Utility */

    private Film getFilmWithRole(Long filmId, Long actorId) {
        Film film = createFilm(filmId);
        Person person = createPerson(actorId);
        Role role = new Role(film, person, "Narrator");
        role.setId(new RoleKey(filmId, actorId));
        film.setCast(Set.of(role));
        return film;
    }

    private CastMember createCastMember(Long personId, String character) {
        return new CastMember() {
            @Override
            public Long getPersonId() {
                return personId;
            }

            @Override
            public String getCharacter() {
                return character;
            }
        };
    }

    private List<CastMember> createCast(Long... peopleIds) {
        return Arrays.stream(peopleIds).map(id -> createCastMember(id, "Character №" + id)).toList();
    }
}
