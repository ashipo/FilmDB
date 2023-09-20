package com.demo.filmdb;

import com.demo.filmdb.role.dtos.FilmRoleDtoInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CastIntegrationTests {

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
        @DisplayName("GET /films/{existing id}/cast, expect 200")
        public void GetCastURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/cast";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.cast.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("GET /films/{not existing id}/cast, expect 404")
        public void GetCastURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetActedBy {
        @Test
        @DisplayName("GET /people/{existing id}/roles, expect 200")
        public void GetDirectedURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people/2/roles";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.roles.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("GET /people/{not existing id}/roles, expect 404")
        public void GetDirectedURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/people/" + NOT_EXISTING_ID + "/roles";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Put {
        @Transactional
        @Test
        @DisplayName("PUT /films/{existing id}/cast valid request, expect 200")
        public void PutCastURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";
            List<String> expectedCharacters = List.of("Dumb", "Dumber");
            List<FilmRoleDtoInput> expectedRoles = List.of(
                    new FilmRoleDtoInput(3L, expectedCharacters.get(0)),
                    new FilmRoleDtoInput(4L, expectedCharacters.get(1)));
            String requestBody = objectMapper.writeValueAsString(expectedRoles);

            mockMvc.perform(put(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.cast.length()").value(2),
                    jsonPath("$._embedded.cast[0].character").value(in(expectedCharacters)),
                    jsonPath("$._embedded.cast[1].character").value(in(expectedCharacters)),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Transactional
        @Test
        @DisplayName("PUT /films/{existing id}/cast empty array in request body, expect 200")
        public void PutCastURI_EmptyArray_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(expectedUri).content("[]"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /films/{existing id}/cast invalid request, expect 400")
        public void PutCastURI_InvalidRequest_Response400() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(expectedUri).content("[1, 2]"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /films/{not existing id}/cast valid request, expect 404")
        public void PutCastURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(put(expectedUri).content("[]"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /films/{existing id}/cast valid request with not existing personId, expect 404")
        public void PutCastURI_NotExistingPersonId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";
            List<FilmRoleDtoInput> expectedRoles = List.of(new FilmRoleDtoInput(NOT_EXISTING_ID, "Vasily"));
            String requestBody = objectMapper.writeValueAsString(expectedRoles);

            mockMvc.perform(put(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete{
        @Transactional
        @Test
        @DisplayName("DELETE /films/{existing id}/cast, expect 204")
        public void DeleteCastURI_ExistingId_Response204() throws Exception {
            String expectedUri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /films/{not existing id}/cast, expect 404")
        public void DeleteCastURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }
}
