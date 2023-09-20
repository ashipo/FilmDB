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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
        @DisplayName("Given not existing id throws 404")
        void NotExistingId_Throws404() {
            final long expectedFilmId = 9L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> filmService.getFilm(expectedFilmId));

            assertThatValid404(thrown, List.of(expectedFilmId));
        }
    }

    @Nested
    class DeleteFilm {
        @Test
        @DisplayName("Deletes existing film by id")
        void ExistingId_DeletesFilm() {
            final long expectedFilmId = 9L;
            given(filmRepository.existsById(anyLong())).willReturn(true);

            filmService.deleteFilm(expectedFilmId);

            verify(filmRepository).deleteById(expectedFilmId);
            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }

        @Test
        @DisplayName("Given not existing id throws 404")
        void NotExistingId_Throws404() {
            final long expectedFilmId = 9L;
            given(filmRepository.existsById(anyLong())).willReturn(false);

            Throwable thrown = catchThrowable(() -> filmService.deleteFilm(expectedFilmId));

            assertThatValid404(thrown, List.of(expectedFilmId));
            verify(filmRepository, never()).deleteById(anyLong());
            verify(roleRepository, never()).deleteById_FilmId(anyLong());
        }
    }

    /* Directors */

    @Nested
    class GetDirectors {
        @Test
        @DisplayName("Given existing id, finds")
        void ExistingId_Gets() {
            final long expectedFilmId = 9L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.getDirectors(expectedFilmId);

            verify(filmRepository).findById(expectedFilmId);
        }

        @Test
        @DisplayName("Given not existing id, throws 404")
        void NotExistingId_Throws404() {
            final long expectedFilmId = 9L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> filmService.getDirectors(expectedFilmId));

            assertThatValid404(thrown, List.of(expectedFilmId));
        }
    }

    @Nested
    class UpdateDirectors {
        @Test
        public void NotEmptyDirectors_SavesDirectors() {
            final long expectedFilmId = 1L;
            final Set<Person> expectedDirectors = Set.of(
                    new Person("Name1", LocalDate.of(1000, 2, 3))
                    , new Person("Name2", LocalDate.of(2000, 4, 5))
            );
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.updateDirectors(expectedFilmId, expectedDirectors);

            ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmArgumentCaptor.capture());
            Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
            assertThat(actualDirectors).isEqualTo(expectedDirectors);
        }

        @Test
        public void NullDirectors_SavesEmptyDirectors() {
            final long expectedFilmId = 1L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.updateDirectors(expectedFilmId, null);

            ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmArgumentCaptor.capture());
            Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
            assertThat(actualDirectors.isEmpty()).isTrue();
        }

        @Test
        public void NotExistingId_Throws404() {
            final long expectedFilmId = 1L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> filmService.updateDirectors(expectedFilmId, null));

            assertThatValid404(thrown, List.of(expectedFilmId));
            verify(filmRepository, never()).save(any());
        }
    }

    @Test
    public void deleteDirectors_ExistingId_SavesEmptyDirectors() {
        final long expectedFilmId = 1L;
        given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

        filmService.deleteDirectors(expectedFilmId);

        ArgumentCaptor<Film> filmArgumentCaptor = ArgumentCaptor.forClass(Film.class);
        verify(filmRepository).save(filmArgumentCaptor.capture());
        Set<Person> actualDirectors = filmArgumentCaptor.getValue().getDirectors();
        assertThat(actualDirectors.isEmpty()).isTrue();
    }

    /* Cast */

    @Nested
    class GetCast {
        @Test
        public void ExistingId_ReturnsRoles() {
            final long expectedFilmId = 3L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));

            filmService.getCast(expectedFilmId);

            verify(filmRepository).findById(expectedFilmId);
        }

        @Test
        public void NotExistingId_Throws404() {
            final long expectedFilmId = 3L;
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> filmService.getCast(expectedFilmId));

            assertThatValid404(thrown, List.of(expectedFilmId));
        }
    }

    @Nested
    class DeleteCast {
        @Test
        public void ExistingId_Deletes() {
            final long expectedFilmId = 1L;
            given(filmRepository.existsById(anyLong())).willReturn(true);

            filmService.deleteCast(expectedFilmId);

            verify(roleRepository).deleteById_FilmId(expectedFilmId);
        }

        @Test
        public void NotExistingId_Throws404() {
            final long expectedFilmId = 1L;
            given(filmRepository.existsById(anyLong())).willReturn(false);

            Throwable thrown = catchThrowable(() -> filmService.deleteCast(expectedFilmId));

            assertThatValid404(thrown, List.of(expectedFilmId));
            verify(roleRepository, never()).deleteById_FilmId(anyLong());
        }
    }

    /* Utility */

    private void assertThatValid404(Throwable thrown, List<Long> notFoundIds){
        assertThatValid404(thrown);
        assertThat(thrown).hasMessageContaining("Could not find film(s) with id(s) " + notFoundIds);
    }
}