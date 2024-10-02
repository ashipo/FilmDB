package com.demo.filmdb.rest;

import com.demo.filmdb.rest.role.dtos.FilmRoleDtoInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.demo.filmdb.rest.Util.NOT_EXISTING_ID;
import static com.demo.filmdb.rest.Util.configureMockMvc;
import static com.demo.filmdb.rest.util.Path.API_PREFIX;
import static com.demo.filmdb.security.SecurityConfig.ROLE_ADMIN;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Cast")
public class CastIntegrationTests {

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
    @DisplayName("Get cast of a film | GET /films/{id}/cast")
    class GetCast {

        @Test
        @DisplayName("Existing id, expect 200")
        public void ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/cast";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.cast.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get roles of a person | GET /people/{id}/roles")
    class GetActedBy {

        @Test
        @DisplayName("Existing id, expect 200")
        public void ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people/2/roles";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.roles.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID + "/roles";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Update cast of a film | PUT /films/{id}/cast")
    class UpdateCast {

        @Test
        @DisplayName("Existing id, valid request, expect 200")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ExistingId_Response200() throws Exception {
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

        @Test
        @DisplayName("Existing id, empty array in request body, expect 200")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void EmptyArray_Response200() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(uri).content("[]"))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
        @ValueSource(strings = {
                "{}",
                "{ \"personId\" : 1}",
                "{ \"character\" : \"test\"}",
                "{ \"personId\" :, \"character\" : \"test\"}",
                "{ \"personId\" : 1, \"character\" :}",
                "{ \"personId\" : 1, \"character\" : \"   \"}"
        })
        @DisplayName("Existing id, invalid request, expect 400")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void InvalidRequest_Response400(String request) throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(uri).content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Not existing id, valid request, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(put(uri).content("[]"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Existing id, valid request with not existing personId, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingPersonId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";
            List<FilmRoleDtoInput> roles = List.of(new FilmRoleDtoInput(NOT_EXISTING_ID, "Vasily"));
            String requestBody = objectMapper.writeValueAsString(roles);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void Unauthorized_Response403() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";
            List<String> characters = List.of("Dumb", "Dumber");
            List<FilmRoleDtoInput> roles = List.of(
                    new FilmRoleDtoInput(3L, characters.get(0)),
                    new FilmRoleDtoInput(4L, characters.get(1)));
            String requestBody = objectMapper.writeValueAsString(roles);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delete cast of a film | DELETE /films/{id}/cast")
    class DeleteCast {

        @Test
        @DisplayName("Existing id, expect 204")
        @Transactional
        @WithMockUser(roles = {ROLE_ADMIN})
        public void ExistingId_Response204() throws Exception {
            String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        @WithMockUser(roles = {ROLE_ADMIN})
        public void NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }
}
