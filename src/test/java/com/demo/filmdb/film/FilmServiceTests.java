package com.demo.filmdb.film;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class FilmServiceTests extends ServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(filmRepository, roleRepository);
    }

    @Test
    void search_ValidArguments_Finds() {
        Specification<Film> expectedSpec = Specification.where(new FilmWithTitle("title"));
        Pageable expectedPageable = PageRequest.of(1, 5);

        filmService.search(expectedSpec, expectedPageable);

        verify(filmRepository).findAll(expectedSpec, expectedPageable);
    }

    @Test
    void getAllFilms_ReturnsFilms() {
        final Pageable expectedPageable = Pageable.unpaged();

        filmService.getAllFilms(expectedPageable);

        verify(filmRepository).findAll(expectedPageable);
    }

    @Test
    void saveFilm_ValidFilm_SavesFilm() {
        Film expectedFilm = new Film("Shining", LocalDate.of(1980, 5, 23),
                "A family heads to an isolated hotel.");

        filmService.saveFilm(expectedFilm);

        verify(filmRepository).save(expectedFilm);
    }

    @Nested
    class GetFilm {
        @Test
        @DisplayName("Gets existing film by id")
        void ExistingId_ReturnsFilm() {
            final long expectedFilmId = 9L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.getFilm(expectedFilmId);

            verify(filmRepository).findById(expectedFilmId);
        }

        @Test
        @DisplayName("Given not existing id returns null")
        void NotExistingId_ReturnsNull() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            Film actual = filmService.getFilm(9L);

            assertThat(actual).isNull();
        }
    }

    @Nested
    class DeleteFilm {
        @Test
        @DisplayName("Deletes existing film")
        void ExistingId_DeletesFilm() {
            final long expectedFilmId = 1L;
            Film expected = new Film();
            expected.setId(expectedFilmId);

            filmService.deleteFilm(expected);

            verify(filmRepository).delete(expected);
            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }
    }

    @Nested
    class DeleteFilmById {
        @Test
        @DisplayName("Deletes existing film")
        void ExistingId_DeletesFilm() {
            final long expected = 1L;

            filmService.deleteFilmById(expected);

            verify(filmRepository).deleteById(expected);
            verify(roleRepository).deleteById_FilmId(expected);
        }
    }

    /* Directors */

    @Nested
    class UpdateDirectors {
        @Test
        public void NotEmptyDirectors_SavesDirectors() {
            final Set<Person> expectedDirectors = Set.of(
                    new Person("Name1", LocalDate.of(1000, 2, 3))
                    , new Person("Name2", LocalDate.of(2000, 4, 5))
            );

            filmService.updateDirectors(new Film(), expectedDirectors);

            ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmArgumentCaptor.capture());
            Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
            assertThat(actualDirectors).isEqualTo(expectedDirectors);
        }

        @Test
        public void NullDirectors_SavesEmptyDirectors() {
            filmService.updateDirectors(new Film(), null);

            ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmArgumentCaptor.capture());
            Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
            assertThat(actualDirectors.isEmpty()).isTrue();
        }
    }

    @Test
    public void deleteDirectors_ExistingId_SavesEmptyDirectors() {
        filmService.deleteDirectors(new Film());

        ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
        verify(filmRepository).save(filmArgumentCaptor.capture());
        Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
        assertThat(actualDirectors.isEmpty()).isTrue();
    }

    /* Cast */

    @Nested
    class DeleteCast {
        @Test
        public void ExistingId_Deletes() {
            final long expectedFilmId = 1L;

            filmService.deleteCast(expectedFilmId);

            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }
    }
}
