package com.demo.filmdb.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository repository;

    @Nested
    @DisplayName("findById_FilmIdAndId_PersonId")
    class FindByFilmIdAndPersonId {

        @Test
        @DisplayName("Given filmId and personId of an existing role, returns the role")
        void ExistingRoleIds_ReturnsRole() {
            long expectedFilmId = 1L;
            long expectedPersonId = 2L;

            Optional<Role> role = repository.findById_FilmIdAndId_PersonId(expectedFilmId, expectedPersonId);

            assertThat(role).hasValueSatisfying((r) -> {
                assertThat(r.getFilm().getId()).isEqualTo(expectedFilmId);
                assertThat(r.getPerson().getId()).isEqualTo(expectedPersonId);
            });
        }

        @Test
        @DisplayName("Given filmId and personId of a not existing role, returns an empty Optional")
        void NotExistingRoleIds_ReturnsEmpty() {
            long filmId = 1L;
            long personId = 5L;

            Optional<Role> role = repository.findById_FilmIdAndId_PersonId(filmId, personId);

            assertThat(role).isEmpty();
        }
    }
}
