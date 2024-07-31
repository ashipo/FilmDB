package com.demo.filmdb.director;

import com.demo.filmdb.ServiceTest;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.util.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("DirectorService")
public class DirectorServiceTests extends ServiceTest {

    private DirectorService directorService;

    @BeforeEach
    void setUp() {
        directorService = new DirectorService(filmRepository, personRepository);
    }

    @Nested
    @DisplayName("setDirector")
    class SetDirector {

        @Test
        @DisplayName("Existing ids, sets")
        void ExistingIds_Sets() {
            Long filmId = 2L;
            Long personId = 3L;
            Person expectedPerson = createPerson(personId);
            given(filmRepository.findById(filmId)).willReturn(Optional.of(createFilm(filmId)));
            given(personRepository.findById(personId)).willReturn(Optional.of(expectedPerson));

            directorService.setDirector(filmId, personId);

            ArgumentCaptor<Film> filmCaptor = ArgumentCaptor.forClass(Film.class);
            verify(filmRepository).save(filmCaptor.capture());
            assertThatCollection(filmCaptor.getValue().getDirectors()).contains(expectedPerson);
        }

        @Test
        @DisplayName("Not existing film id, throws EntityNotFoundException")
        void NotExistingFilmId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.setDirector(1L, 2L)
            );
        }

        @Test
        @DisplayName("Not existing person id, throws EntityNotFoundException")
        void NotExistingPersonId_Throws() {
            given(filmRepository.findById(anyLong())).willReturn(Optional.of(new Film()));
            given(personRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                    directorService.setDirector(1L, 2L)
            );
        }
    }
}
