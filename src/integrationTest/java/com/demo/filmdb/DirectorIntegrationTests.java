package com.demo.filmdb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.StringJoiner;

import static com.demo.filmdb.Utils.*;
import static com.demo.filmdb.util.Path.API_PREFIX;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Directors")
public class DirectorIntegrationTests {

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
    @DisplayName("GET directors for a film")
    class Get {

        @Test
        @DisplayName("Existing id, expect 200")
        public void ExistingId_Response200() throws Exception {
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
        @DisplayName("Not existing id, expect 404")
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET films directed by a person")
    class GetDirectedBy {

        @Test
        @DisplayName("Existing id, expect 200")
        public void ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people/3/films_directed";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.films.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID + "/films_directed";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT directors for a film")
    class Put {

        @Test
        @DisplayName("Existing ids, valid request, expect 200")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ValidRequest_Response200() throws Exception {
            String expectedUri = API_PREFIX + "/films/2/directors";
            final List<Long> expectedPeopleIds = List.of(1L, 3L);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            expectedPeopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.people.length()").value(2),
                    jsonPath("$._embedded.people[0].id").value(in(expectedPeopleIds), Long.class),
                    jsonPath("$._embedded.people[1].id").value(in(expectedPeopleIds), Long.class),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Existing film id, invalid request, expect 400")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void InvalidRequest_Response400() throws Exception {
            final String uri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(put(uri).content("[abc, zxc]"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Not existing film id, existing people ids, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingFilmId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(put(uri).content("[1]"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Existing film id, not existing people ids, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingPeopleId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/2/directors";
            final List<Long> peopleIds = List.of(NOT_EXISTING_ID, NOT_EXISTING_ID + 1);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            peopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, existing ids, expect 403")
        public void Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/films/2/directors";
            final List<Long> peopleIds = List.of(1L, 3L);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            peopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, not existing ids, expect 403")
        public void UnauthorizedNotExistingIds_Response403() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";
            final List<Long> peopleIds = List.of(NOT_EXISTING_ID, NOT_EXISTING_ID + 1);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            peopleIds.forEach(id -> joiner.add(id.toString()));
            String requestBody = joiner.toString();

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, invalid request, expect 403")
        public void UnauthorizedInvalidRequest_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(put(uri).content("[abc, zxc]"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE directors for a film")
    class Delete {

        @Test
        @DisplayName("Existing id, expect 204")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ExistingId_Response204() throws Exception {
            String uri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, existing id, expect 403")
        public void UnauthorizedExistingId_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1/directors";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthorized, not existing id, expect 403")
        public void UnauthorizedNotExistingId_Response403() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/directors";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }
}
