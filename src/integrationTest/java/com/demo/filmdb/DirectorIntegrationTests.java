package com.demo.filmdb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.StringJoiner;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class DirectorIntegrationTests {

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
        @DisplayName("GET /films/{existing id}/directors, expect 200")
        public void GetDirectorsURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/directors";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.people.length()").value(2),
                    jsonPath("$._embedded.people[0].name").value(containsString("Russo")),
                    jsonPath("$._embedded.people[1].name").value(containsString("Russo")),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("GET /films/{not existing id}/directors, expect 404")
        public void GetDirectorsURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetDirectedBy {
        @Test
        @DisplayName("GET /people/{existing id}/films_directed, expect 200")
        public void GetDirectedURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people/3/films_directed";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.films.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("GET /people/{not existing id}/films_directed, expect 404")
        public void GetDirectedURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/people/" + NOT_EXISTING_ID + "/films_directed";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Put {
        @Transactional
        @Test
        @DisplayName("PUT //films/{existing id}/directors existing people ids, expect 200")
        public void PutDirectorsURI_ValidRequest_Response200() throws Exception {
            String expectedUri = API_PREFIX + "/films/2/directors";
            final List<Long> expectedPeopleIds = List.of(1L, 3L);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            expectedPeopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.people.length()").value(2),
                    jsonPath("$._embedded.people[0].id"). value(in(expectedPeopleIds), Long.class),
                    jsonPath("$._embedded.people[1].id").value(in(expectedPeopleIds), Long.class),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("PUT /films/{existing id}/directors invalid request, expect 400")
        public void PutDirectorsURI_InvalidRequest_Response400() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(put(expectedUri).content("[abc, zxc]"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /films/{not existing id}/directors existing people ids, expect 404")
        public void PutDirectorsURI_NotExistingFilmId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(put(expectedUri).content("[1]"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /films/{existing id}/directors not existing people ids, expect 404")
        public void PutDirectorsURI_NotExistingPeopleId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/directors";
            final List<Long> expectedPeopleIds = List.of(NOT_EXISTING_ID, NOT_EXISTING_ID + 1);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            expectedPeopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete{
        @Transactional
        @Test
        @DisplayName("DELETE /films/{existing id}/directors, expect 204")
        public void DeleteDirectorsURI_ExistingId_Response204() throws Exception {
            String expectedUri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /films/{not existing id}/directors, expect 404")
        public void DeleteDirectorsURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }
}
