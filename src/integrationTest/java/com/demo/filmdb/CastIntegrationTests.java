package com.demo.filmdb;

import com.demo.filmdb.role.dtos.FilmRoleDtoInput;
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

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
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
    @DisplayName("GET cast for a film")
    class Get {
        @Test
        @DisplayName("Existing id, expect 200")
        public void GetCastURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/cast";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.cast.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void GetCastURI_NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET roles for a person")
    class GetActedBy {
        @Test
        @DisplayName("Existing id, expect 200")
        public void GetDirectedURI_ExistingId_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people/2/roles";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.roles.length()").value(2),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void GetDirectedURI_NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID + "/roles";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT cast for a film")
    class Put {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, valid request, expect 200")
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
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, empty array in request body, expect 200")
        public void PutCastURI_EmptyArray_Response200() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(uri).content("[]"))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest(name = "{arguments}")
        @ValueSource(strings = {
                "{}",
                "{ \"personId\" : 1}",
                "{ \"character\" : \"test\"}",
                "{ \"personId\" :, \"character\" : \"test\"}",
                "{ \"personId\" : 1, \"character\" :}",
                "{ \"personId\" : 1, \"character\" : \"   \"}"
        })
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, invalid request, expect 400")
        public void PutCastURI_InvalidRequest_Response400(String request) throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(put(uri).content(request))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing id, valid request, expect 404")
        public void PutCastURI_NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(put(uri).content("[]"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, valid request with not existing personId, expect 404")
        public void PutCastURI_NotExistingPersonId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";
            List<FilmRoleDtoInput> roles = List.of(new FilmRoleDtoInput(NOT_EXISTING_ID, "Vasily"));
            String requestBody = objectMapper.writeValueAsString(roles);

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void PutCastURI_Unauthorized_Response403() throws Exception {
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
    @DisplayName("DELETE cast for a film")
    class Delete {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, expect 204")
        public void DeleteCastURI_ExistingId_Response204() throws Exception {
            String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing id, expect 404")
        public void DeleteCastURI_NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void DeleteCastURI_Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }
}
