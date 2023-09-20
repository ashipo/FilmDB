package com.demo.filmdb;

import com.demo.filmdb.film.dtos.FilmDtoInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class FilmIntegrationTests {

    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    static void beforeAll() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = configureMockMvc(wac);
    }

    @Nested
    class Get {
        @Test
        @DisplayName("GET /films, expect 200")
        public void GetFilmsURI_MockMVC_Response200() throws Exception {
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
        @DisplayName("GET /films/{existing id}, expect 200")
        public void GetFilmURI_ExistingId_Response200() throws Exception {
            final long expectedFilmId = 1L;
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
        @DisplayName("GET /films/{not existing id}, expect 404")
        public void GetFilmURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /films/{invalid id}, expect 400")
        public void GetFilmURI_InvalidId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/abc";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Post {
        @Transactional
        @Test
        @DisplayName("POST /films valid request, expect 201")
        public void PostFilmsURI_ValidBody_Response201() throws Exception {
            final String expectedUri = API_PREFIX + "/films";
            FilmDtoInput expectedFilm = new FilmDtoInput("Terminator",
                    LocalDate.of(1984, 10, 26), "A human soldier is sent from 2029 to 1984");
            String requestBody = objectMapper.writeValueAsString(expectedFilm);

            mockMvc.perform(post(expectedUri).content(requestBody)).andExpectAll(
                    status().isCreated(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(4),
                    jsonPath("$.title").value(expectedFilm.title()),
                    jsonPath("$.['release date']").value(expectedFilm.releaseDate().toString()),
                    jsonPath("$.synopsis").value(expectedFilm.synopsis()));
        }

        @Test
        @DisplayName("POST /films invalid request, expect 400")
        public void PostFilmsURI_InvalidBody_Response400() throws Exception {
            final String expectedUri = API_PREFIX + "/films";

            mockMvc.perform(post(expectedUri).content("{\"title\": \"test\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Put {
        @Transactional
        @Test
        @DisplayName("PUT /films/{existing id} valid request, expect 200")
        public void PutFilmURI_ValidRequest_Response200() throws Exception {
            long expectedFilmId = 2L;
            String expectedUri = API_PREFIX + "/films/" + expectedFilmId;
            String expectedTitle = "Matrix";
            String expectedReleaseDate = "1999-11-11";
            String expectedSynopsis = "Once upon a time";
            String requestBody = objectMapper.createObjectNode()
                    .put("id", expectedFilmId + 1)  //expected to be ignored
                    .put("title", expectedTitle)
                    .put("releaseDate", expectedReleaseDate)
                    .put("synopsis", expectedSynopsis)
                    .toString();

            mockMvc.perform(put(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(expectedFilmId),
                    jsonPath("$.title").value(expectedTitle),
                    jsonPath("$.['release date']").value(expectedReleaseDate),
                    jsonPath("$.synopsis").value(expectedSynopsis));
        }

        @Test
        @DisplayName("PUT /films/{existing id} invalid request, expect 400")
        public void PutFilmURI_InvalidRequest_Response400() throws Exception {
            long expectedFilmId = 2L;
            String expectedUri = API_PREFIX + "/films/" + expectedFilmId;
            String requestBody = objectMapper.createObjectNode()
                    .put("id", expectedFilmId + 1)  //expected to be ignored
                    .put("not_a_field", "random_data")
                    .toString();

            mockMvc.perform(put(expectedUri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /films/{not existing id} valid request, expect 404")
        public void PutFilmURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(put(expectedUri).content("{\"title\": \"Title\", \"releaseDate\": \"1999-11-11\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete {
        @Transactional
        @Test
        @DisplayName("DELETE /films/{existing id}, expect 204")
        public void DeleteFilmURI_ExistingId_Response204() throws Exception {
            String expectedUri = API_PREFIX + "/films/1";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /films/{not existing id}, expect 404")
        public void DeleteFilmURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }
}
