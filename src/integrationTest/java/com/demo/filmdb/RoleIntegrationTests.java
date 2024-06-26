package com.demo.filmdb;

import com.demo.filmdb.role.dtos.FilmRoleDtoInput;
import com.demo.filmdb.role.dtos.RoleDtoInput;
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

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Role")
public class RoleIntegrationTests {

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
    @DisplayName("POST")
    class Post {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, valid request, expect 201")
        public void PostCastURI_ExistingId_Response201() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";
            FilmRoleDtoInput expectedRole = new FilmRoleDtoInput(3L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(post(expectedUri).content(requestBody)).andExpectAll(
                    status().isCreated(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.film").value("Thor: Ragnarok"),
                    jsonPath("$.actor").value("Taika Waititi"),
                    jsonPath("$.character").value(expectedRole.character()),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing film id, valid request, expect 404")
        public void PostCastURI_NotExistingFilmId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";
            FilmRoleDtoInput role = new FilmRoleDtoInput(3L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, valid request with not existing personId, expect 404")
        public void PostCastURI_NotExistingPersonId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";
            FilmRoleDtoInput role = new FilmRoleDtoInput(NOT_EXISTING_ID, "Cameo");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, valid request with existing role, expect 409")
        public void PostCastURI_ExistingRole_Response409() throws Exception {
            final String uri = API_PREFIX + "/films/2/cast";
            FilmRoleDtoInput role = new FilmRoleDtoInput(2L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isConflict());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "{}",
                "{ \"personId\" : 1}",
                "{ \"character\" : \"test\"}",
                "{ \"personId\" :, \"character\" : \"test\"}",
                "{ \"personId\" : 1, \"character\" :}"
        })
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, invalid request, expect 400")
        public void PostCastURI_InvalidRequest_Response400(String requestBody) throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void PostCastURI_Unauthorized_Response403() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast";
            FilmRoleDtoInput role = new FilmRoleDtoInput(3L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET")
    class Get {
        @Test
        @DisplayName("Existing film id, existing person id, expect 200")
        public void GetRoleURI_ExistingIds_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/films/3/cast/1";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.film").value("Jojo Rabbit"),
                    jsonPath("$.actor").value("Scarlett Johansson"),
                    jsonPath("$.character").value("Rosie"),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing film id, existing person id, expect 404")
        public void GetRoleURI_NotExistingFilmId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/1";

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Existing film id, not existing person id, expect 404")
        public void GetRoleURI_NotExistingPersonId_Response404() throws Exception {
            final String uri = API_PREFIX + "/films/1/cast/" + NOT_EXISTING_ID;

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH")
    class Patch {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, existing person id, expect 200")
        public void PatchRoleURI_ExistingIds_Response200() throws Exception {
            String expectedUri = API_PREFIX + "/films/3/cast/3";
            RoleDtoInput expectedRole = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(patch(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.character").value(expectedRole.character()),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, existing person id, invalid request, expect 400")
        public void PatchRoleURI_InvalidRequest_Response400() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/3";

            mockMvc.perform(patch(uri).content("{\"title\": \"Title\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing film id, existing person id, expect 404")
        public void PatchRoleURI_NotExistingFilmId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/3";
            RoleDtoInput role = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(patch(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, not existing person id, expect 404")
        public void PatchRoleURI_NotExistingPersonId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/" + NOT_EXISTING_ID;
            RoleDtoInput role = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(patch(uri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void PatchRoleURI_Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/3";
            RoleDtoInput role = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(role);

            mockMvc.perform(patch(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE")
    class Delete {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, existing person id, expect 204")
        public void DeleteRoleURI_ExistingIds_Response204() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/3";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing film id, existing person id, expect 404")
        public void DeleteRoleURI_NotExistingFilmId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/3";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing film id, not existing person id, expect 404")
        public void DeleteRoleURI_NotExistingPersonId_Response404() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void DeleteRoleURI_Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/films/3/cast/3";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }
}
