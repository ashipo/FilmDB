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

import java.util.*;

import static com.demo.filmdb.util.Creators.*;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
            given(roleRepository.findById_FilmIdAndId_PersonId(expectedFilmId, expectedPersonId)).willReturn(Optional.of(expectedRole));

            var actual = roleService.getRole(expectedFilmId, expectedPersonId);

            verify(roleRepository).findById_FilmIdAndId_PersonId(expectedFilmId, expectedPersonId);
            assert actual.isPresent();
            assertThat(actual.get()).isEqualTo(expectedRole);
        }

        @Test
        @DisplayName("Given ids of a non existing role, returns null")
        void NotExistingIds_ReturnsNull() {
            given(roleRepository.findById_FilmIdAndId_PersonId(anyLong(), anyLong())).willReturn(Optional.empty());

            var actual = roleService.getRole(11L, 11L);

            assertThat(actual).isEmpty();
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
            final Role.Id expectedId = new Role.Id(filmId, personId);

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
            given(filmService.getFilm(expectedFilmId)).willReturn(Optional.of(createFilm(expectedFilmId)));
            given(personService.getPerson(expectedPersonId)).willReturn(Optional.of(createPerson(expectedPersonId)));
            given(roleRepository.existsById(any())).willReturn(false);
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            Role actual = roleService.createRole(expectedFilmId, expectedPersonId, expectedCharacter);

            assertThat(actual.getFilm().getId()).as("Film id").isEqualTo(expectedFilmId);
            assertThat(actual.getPerson().getId()).as("Person id").isEqualTo(expectedPersonId);
            assertThat(actual.getCharacter()).as("Character").isEqualTo(expectedCharacter);
        }

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        void NotExistingFilmId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.createRole(1L, 1L, "Char")
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(Optional.of(new Film()));
            given(personService.getPerson(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.createRole(1L, 1L, "Char")
            );
        }

        @Test
        @DisplayName("Existing role, throws EntityAlreadyExistsException")
        void ExistingRole_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(Optional.of(new Film()));
            given(personService.getPerson(anyLong())).willReturn(Optional.of(new Person()));
            given(roleRepository.existsById(any())).willReturn(true);

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
            given(roleRepository.findById_FilmIdAndId_PersonId(expectedFilmId, expectedPersonId)).willReturn(Optional.of(originalRole));
            when(roleRepository.save(any())).then(AdditionalAnswers.returnsFirstArg());

            Role actual = roleService.updateRole(expectedFilmId, expectedPersonId, expectedCharacter);

            assertThat(actual.getFilm().getId()).as("Film id").isEqualTo(expectedFilmId);
            assertThat(actual.getPerson().getId()).as("Person id").isEqualTo(expectedPersonId);
            assertThat(actual.getCharacter()).as("Character").isEqualTo(expectedCharacter);
        }

        @Test
        @DisplayName("Not existing role, throws EntityNotFoundException")
        void NotExistingRole_Throws() {
            given(roleRepository.findById_FilmIdAndId_PersonId(anyLong(), anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateRole(1L, 1L, "Lawrence")
            );
        }
    }

    @Nested
    @DisplayName("roleExists")
    class RoleExists {

        @Test
        @DisplayName("Existing role, returns true")
        void ExistingRole_ReturnsTrue() {
            final Long filmId = 1L;
            final Long personId = 2L;
            given(roleRepository.existsById(any())).willReturn(true);

            boolean actual = roleService.roleExists(filmId, personId);

            assertThat(actual).isTrue();
        }

        @Test
        @DisplayName("Not existing role, returns false")
        void NotExistingRole_ReturnsFalse() {
            final Long filmId = 1L;
            final Long personId = 2L;
            given(roleRepository.existsById(any())).willReturn(false);

            boolean actual = roleService.roleExists(filmId, personId);

            assertThat(actual).isFalse();
        }
    }

    /* Cast */

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
            given(filmService.getFilm(filmId)).willReturn(Optional.of(createFilm(filmId)));
            given(personService.getPerson(personId)).willReturn(Optional.of(createPerson(personId)));
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
            final Role oldRole = createRole(filmId, personId, "Joker");
            Film film = mock(Film.class);
            when(film.getCast()).thenReturn(Set.of(oldRole));
            given(filmService.getFilm(filmId)).willReturn(Optional.of(film));
            given(roleRepository.findById_FilmIdAndId_PersonId(filmId, personId)).willReturn(Optional.of(oldRole));
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
            final Role oldRole = createRole(filmId, personId, "Catwoman");
            Film film = mock(Film.class);
            when(film.getCast()).thenReturn(Set.of(oldRole));
            given(filmService.getFilm(filmId)).willReturn(Optional.of(film));

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
            final Role oldRole = createRole(filmId, personId, "Bane");
            Film film = mock(Film.class);
            when(film.getCast()).thenReturn(Set.of(oldRole));
            given(filmService.getFilm(filmId)).willReturn(Optional.of(film));

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
            given(filmService.getFilm(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateCast(1L, createCast(1L))
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmService.getFilm(anyLong())).willReturn(Optional.of(new Film()));
            given(personService.getPerson(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    roleService.updateCast(1L, createCast(1L))
            );
        }
    }

    @Nested
    @DisplayName("deleteCast")
    class DeleteCast {

        @Test
        @DisplayName("Deletes correctly")
        public void ExistingId_Deletes() {
            final Long expectedFilmId = 1L;

            roleService.deleteCast(expectedFilmId);

            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }
    }

    /* Utility */

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
