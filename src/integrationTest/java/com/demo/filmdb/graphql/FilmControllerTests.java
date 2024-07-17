package com.demo.filmdb.graphql;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.graphql.inputs.FilmInput;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.demo.filmdb.graphql.Util.*;
import static graphql.ErrorType.ValidationError;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

@GraphQlTest({FilmController.class, TestConfigurer.class})
@DisplayName("GraphQL Film")
public class FilmControllerTests {

    @Autowired
    GraphQlTester graphQlTester;

    @MockBean
    private FilmService filmService;
    @MockBean
    private PersonService personService;

    @Nested
    @DisplayName(FILMS)
    class Films {

        @Test
        @DisplayName("No arguments, runs successfully")
        void NoArguments_RunsSuccessfully() {
            graphQlTester
                    .documentName(FILMS)
                    .executeAndVerify();
        }

        @Test
        @DisplayName("Arbitrary paging, requests correct page")
        void ArbitraryPaging_RequestsCorrectPage() {
            final Integer pageNumber = 3;
            final Integer pageSize = 14;

            graphQlTester
                    .documentName(FILMS)
                    .variable("page", pageNumber)
                    .variable("pageSize", pageSize)
                    .executeAndVerify();

            ArgumentCaptor<Pageable> requestedPageable = ArgumentCaptor.forClass(Pageable.class);
            verify(filmService).getAllFilms(requestedPageable.capture());
            assertThat(requestedPageable.getValue().getPageNumber()).isEqualTo(pageNumber);
            assertThat(requestedPageable.getValue().getPageSize()).isEqualTo(pageSize);
        }

        @Test
        @DisplayName("Arbitrary paging, correct response size")
        void CustomPaging_RequestsCorrectPage() {
            final int pageSize = 5;
            List<Film> films = new ArrayList<>(pageSize);
            for (int i = 0; i < pageSize; i++) {
                films.add(new Film());
            }
            Page<Film> filmsPage = new PageImpl<>(films);
            given(filmService.getAllFilms(any())).willReturn(filmsPage);

            graphQlTester
                    .documentName(FILMS)
                    .variable("pageSize", pageSize)
                    .execute()
                    .path(FILMS)
                    .entityList(Film.class)
                    .hasSize(pageSize);
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidArgument_ValidationError() {
            graphQlTester
                    .documentName(FILMS)
                    .variable("page", "one")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(FILM_BY_ID)
    class FilmById {

        @Test
        @DisplayName("Existing id, correct response")
        void ExistingId_CorrectResponse() {
            Long id = 37L;
            Film expectedFilm = new Film("title", LocalDate.now(), "synopsis");
            expectedFilm.setId(id);
            given(filmService.getFilm(anyLong())).willReturn(expectedFilm);

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", id)
                    .execute()
                    .path(FILM_BY_ID)
                    .entity(Film.class)
                    .matches(film -> film.getId().equals(id))
                    .matches(film -> film.getTitle().equals(expectedFilm.getTitle()))
                    .matches(film -> film.getReleaseDate().equals(expectedFilm.getReleaseDate()))
                    .matches(film -> film.getSynopsis().equals(expectedFilm.getSynopsis()));
        }

        @Test
        @DisplayName("Non existing id, null response")
        void NotExistingId_NullResponse() {
            given(filmService.getFilm(anyLong())).willReturn(null);

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", 1)
                    .execute()
                    .path(FILM_BY_ID)
                    .valueIsNull();
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            given(filmService.getFilm(anyLong())).willReturn(new Film());

            graphQlTester
                    .documentName(FILM_BY_ID)
                    .variable("id", "Invalid ID")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(DELETE_FILM)
    class DeleteFilm {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            Long expectedId = 1L;

            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable("id", expectedId)
                    .execute()
                    .path(DELETE_FILM)
                    .entity(Long.class).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .variable("id", "five")
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }

        @Test
        @DisplayName("Missing input, validation error")
        void MissingInput_ValidationError() {
            graphQlTester
                    .documentName(DELETE_FILM)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(CREATE_FILM)
    class CreateFilm {

        @Test
        @DisplayName("Valid input, correct response")
        void ValidInput_CorrectResponse() {
            FilmInput input = getValidFilmInput();
            Map<String, Object> inputMap = getFilmInputMap(input);

            given(filmService.saveFilm(any())).willReturn(new Film(input.title(), input.releaseDate(), input.synopsis()));

            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable("filmInput", inputMap)
                    .execute()
                    .path(CREATE_FILM)
                    .entity(Film.class)
                    .matches(film -> film.getTitle().equals(input.title()))
                    .matches(film -> film.getReleaseDate().equals(input.releaseDate()))
                    .matches(film -> film.getSynopsis().equals(input.synopsis()));
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            Map<String, Object> invalidInput = Map.of("title", "Title");

            graphQlTester
                    .documentName(CREATE_FILM)
                    .variable("filmInput", invalidInput)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_FILM)
    class UpdateFilm {

        @Test
        @DisplayName("Valid input, saves correctly")
        void ValidInput_CorrectResponse() {
            Long expectedId = 1L;
            Film filmToUpdate = new Film();
            filmToUpdate.setId(expectedId);
            given(filmService.getFilm(anyLong())).willReturn(filmToUpdate);
            given(filmService.saveFilm(any())).willReturn(new Film());

            FilmInput input = getValidFilmInput();
            Map<String, Object> inputMap = getFilmInputMap(input);
            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable("id", expectedId)
                    .variable("filmInput", inputMap)
                    .execute()
                    .path(UPDATE_FILM)
                    .hasValue();

            ArgumentCaptor<Film> savedFilm = ArgumentCaptor.forClass(Film.class);
            verify(filmService).saveFilm(savedFilm.capture());
            assertThat(savedFilm.getValue().getId()).isEqualTo(expectedId);
            assertThat(savedFilm.getValue().getTitle()).isEqualTo(input.title());
            assertThat(savedFilm.getValue().getReleaseDate()).isEqualTo(input.releaseDate());
            assertThat(savedFilm.getValue().getSynopsis()).isEqualTo(input.synopsis());
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            Map<String, Object> invalidInput = Map.of("title", "Title");

            graphQlTester
                    .documentName(UPDATE_FILM)
                    .variable("id", 1)
                    .variable("filmInput", invalidInput)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }

    @Nested
    @DisplayName(UPDATE_DIRECTORS)
    class UpdateDirectors {

        @Captor
        ArgumentCaptor<List<Person>> peopleCaptor;

        @Test
        @DisplayName("Valid input, updates correctly")
        void ValidInput_CorrectResponse() {
            List<Long> requestedPeopleIds = List.of(3L, 7L);
            List<Person> foundPeople = new ArrayList<>();
            for (Long l : requestedPeopleIds) {
                Person person = new Person();
                person.setId(l);
                foundPeople.add(person);
            }
            given(personService.getPeople(any())).willReturn(foundPeople);
            given(filmService.getFilm(anyLong())).willReturn(new Film());

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 5L)
                    .variable("directorsIds", requestedPeopleIds)
                    .executeAndVerify();

            verify(filmService).updateDirectors(any(), peopleCaptor.capture());
            assertThat(peopleCaptor.getValue().size()).isEqualTo(requestedPeopleIds.size());
        }

        @Test
        @DisplayName("Not exising people ids, not found error")
        void NotExistingPeopleIds_NotFoundError() {
            List<Long> requestedPeopleIds = List.of(3L, 7L);
            List<Person> foundPeople = new ArrayList<>();
            Person person1 = new Person();
            person1.setId(3L);
            foundPeople.add(person1);
            given(personService.getPeople(any())).willReturn(foundPeople);
            given(filmService.getFilm(anyLong())).willReturn(new Film());

            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("filmId", 5L)
                    .variable("directorsIds", requestedPeopleIds)
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == NOT_FOUND)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }

        @Test
        @DisplayName("Invalid input, validation error")
        void InvalidInput_ValidationError() {
            graphQlTester
                    .documentName(UPDATE_DIRECTORS)
                    .variable("directorsIds", List.of(3L))
                    .execute()
                    .errors()
                    .expect(responseError -> responseError.getErrorType() == ValidationError)
                    .verify()
                    .path(DATA)
                    .pathDoesNotExist();
        }
    }
}
