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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class RoleIntegrationTests {

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
    class Post {
        @Transactional
        @Test
        @DisplayName("POST /films/{existing id}/cast valid request, expect 201")
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
        @DisplayName("POST /films/{not existing id}/cast valid request, expect 404")
        public void PostCastURI_NotExistingFilmId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast";
            FilmRoleDtoInput expectedRole = new FilmRoleDtoInput(3L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(post(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /films/{existing id}/cast valid request with not existing personId, expect 404")
        public void PostCastURI_NotExistingPersonId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";
            FilmRoleDtoInput expectedRole = new FilmRoleDtoInput(NOT_EXISTING_ID, "Cameo");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(post(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /films/{existing id}/cast valid request with existing role, expect 409")
        public void PostCastURI_ExistingRole_Response409() throws Exception {
            final String expectedUri = API_PREFIX + "/films/2/cast";
            FilmRoleDtoInput expectedRole = new FilmRoleDtoInput(2L, "Cameo");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(post(expectedUri).content(requestBody))
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
        @DisplayName("POST /films/{existing id}/cast invalid request, expect 400")
        public void PostCastURI_InvalidRequest_Response400(String requestBody) throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast";

            mockMvc.perform(post(expectedUri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Get {
        @Test
        @DisplayName("GET /films/{existing id}/cast/{existing id}, expect 200")
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
        @DisplayName("GET /films/{not existing id}/cast/{existing id}, expect 404")
        public void GetRoleURI_NotExistingFilmId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/1";

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /films/{existing id}/cast/{not existing id}, expect 404")
        public void GetRoleURI_NotExistingPersonId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/films/1/cast/" + NOT_EXISTING_ID;

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Patch {
        @Transactional
        @Test
        @DisplayName("PATCH /films/{existing id}/cast/{existing id}, expect 200")
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
        @DisplayName("PATCH /films/{not existing id}/cast/{existing id}, expect 400")
        public void PatchRoleURI_InvalidRequest_Response400() throws Exception {
            String expectedUri = API_PREFIX + "/films/3/cast/3";

            mockMvc.perform(patch(expectedUri).content("{\"title\": \"Title\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PATCH /films/{not existing id}/cast/{existing id}, expect 404")
        public void PatchRoleURI_NotExistingFilmId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/3";
            RoleDtoInput expectedRole = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(patch(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PATCH /films/{existing id}/cast/{not existing id}, expect 404")
        public void PatchRoleURI_NotExistingPersonId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/3/cast/" + NOT_EXISTING_ID;
            RoleDtoInput expectedRole = new RoleDtoInput("Borat");
            String requestBody = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(patch(expectedUri).content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete {
        @Transactional
        @Test
        @DisplayName("DELETE /films/{existing id}/cast/{existing id}, expect 204")
        public void DeleteRoleURI_ExistingIds_Response204() throws Exception {
            String expectedUri = API_PREFIX + "/films/3/cast/3";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /films/{not existing id}/cast/{existing id}, expect 404")
        public void DeleteRoleURI_NotExistingFilmId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/" + NOT_EXISTING_ID + "/cast/3";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /films/{existing id}/cast/{not existing id}, expect 404")
        public void DeleteRoleURI_NotExistingPersonId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/films/3/cast/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }
}
