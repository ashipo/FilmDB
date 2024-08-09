package com.demo.filmdb;

import com.demo.filmdb.film.dtos.FilmDtoInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Film")
public class FilmIntegrationTests {

    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    static void beforeAll() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = configureMockMvc(wac, springSecurity());
    }

    @Nested
    @DisplayName("GET")
    class Get {

        @Test
        @DisplayName("All films, expect 200")
        public void Films_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.films.length()").value(3),
                    jsonPath("$..films[0].title").value("Thor: Ragnarok"),
                    jsonPath("$..['films'][2]['release date']").value("2019-09-08"),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Existing id, expect 200")
        public void ExistingId_Response200() throws Exception {
            final Long expectedFilmId = 1L;
            final String expectedUri = API_PREFIX + "/films/" + expectedFilmId;

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(expectedFilmId),
                    jsonPath("$.title").value("Thor: Ragnarok"),
                    jsonPath("$.['release date']").value("2017-10-10"),
                    jsonPath("$.synopsis").value(containsString("Asgard")),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST")
    class Post {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.FilmIntegrationTests#validFilmInputs")
        @DisplayName("Valid request, expect 201")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ValidRequest_Response201(FilmDtoInput filmInput) throws Exception {
            final String uri = API_PREFIX + "/films";
            String requestBody = objectMapper.writeValueAsString(filmInput);

            mockMvc.perform(post(uri).content(requestBody)).andExpectAll(
                    status().isCreated(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(filmInput.title()),
                    jsonPath("$.['release date']").value(filmInput.releaseDate().toString()),
                    jsonPath("$.synopsis").value(filmInput.synopsis()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.FilmIntegrationTests#invalidFilmInputs")
        @DisplayName("Invalid request, expect 400")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void InvalidRequest_Response400(FilmDtoInput filmInput) throws Exception {
            final String uri = API_PREFIX + "/films";
            String requestBody = objectMapper.writeValueAsString(filmInput);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unauthorized, valid request, expect 403")
        public void UnauthorizedValidRequest_Response403() throws Exception {
            final String uri = API_PREFIX + "/films";
            String requestBody = objectMapper.writeValueAsString(VALID_FILM_INPUT);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, invalid request, expect 403")
        public void UnauthorizedInvalidRequest_Response403() throws Exception {
            final String uri = API_PREFIX + "/films";

            mockMvc.perform(post(uri).content(INVALID_REQUEST_BODY))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT")
    class Put {

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.FilmIntegrationTests#validFilmInputs")
        @DisplayName("Existing id, valid request, expect 200")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ValidRequest_Response200(FilmDtoInput filmInput) throws Exception {
            String uri = API_PREFIX + "/films/1";
            String requestBody = objectMapper.writeValueAsString(filmInput);

            mockMvc.perform(put(uri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.title").value(filmInput.title()),
                    jsonPath("$.['release date']").value(filmInput.releaseDate().toString()),
                    jsonPath("$.synopsis").value(filmInput.synopsis()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.demo.filmdb.FilmIntegrationTests#invalidFilmInputs")
        @DisplayName("Existing id, invalid request, expect 400")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void InvalidRequest_Response400(FilmDtoInput filmInput) throws Exception {
            String uri = API_PREFIX + "/films/1";
            String requestBody = objectMapper.writeValueAsString(filmInput);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Not existing id, valid request, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID;
            String requestBody = objectMapper.writeValueAsString(VALID_FILM_INPUT);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, valid request, expect 403")
        public void UnauthorizedValidRequest_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1";
            String requestBody = objectMapper.writeValueAsString(VALID_FILM_INPUT);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, invalid request, expect 403")
        public void UnauthorizedInvalidRequest_Response403() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(put(uri).content(INVALID_REQUEST_BODY))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE")
    class Delete {

        @Test
        @DisplayName("Existing id, expect 204")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ExistingId_Response204() throws Exception {
            String uri = API_PREFIX + "/films/1";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, existing id, expect 403")
        public void UnauthorizedExistingId_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, not existing id, expect 403")
        public void UnauthorizedNotExistingId_Response403() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }

    private static Stream<Arguments> validFilmInputs() {
        return Stream.of(
                arguments(named("All fields", new FilmDtoInput("Title", LocalDate.now(), "Sy"))),
                arguments(named("Null synopsis", new FilmDtoInput("Title", LocalDate.now(), null)))
        );
    }

    private static Stream<Arguments> invalidFilmInputs() {
        return Stream.of(
                arguments(named("Null title", new FilmDtoInput(null, LocalDate.now(), "Sy"))),
                arguments(named("Null release date", new FilmDtoInput("Title", null, "Sy"))),
                arguments(named("Empty title", new FilmDtoInput("", LocalDate.now(), "Sy"))),
                arguments(named("Blank title", new FilmDtoInput("   ", LocalDate.now(), "Sy")))
        );
    }
}
