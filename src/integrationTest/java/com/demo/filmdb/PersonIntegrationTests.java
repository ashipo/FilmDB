package com.demo.filmdb;

import com.demo.filmdb.person.dtos.PersonDtoInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Person")
public class PersonIntegrationTests {

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
        @DisplayName("All people, expect 200")
        public void GetPeopleURI_MockMVC_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.people.length()").value(5),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Existing id, expect 200")
        public void GetPeopleURI_ExistingId_Response200() throws Exception {
            final long expectedPersonId = 1L;
            final String expectedUri = API_PREFIX + "/people/" + expectedPersonId;

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(expectedPersonId),
                    jsonPath("$.name").value("Scarlett Johansson"),
                    jsonPath("$.['date of birth']").value("1984-11-22"),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("Not existing id, expect 404")
        public void GetPeopleURI_NotExistingId_Response404() throws Exception {
            final String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(get(uri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST")
    class Post {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Valid request, expect 201")
        public void PostPeopleURI_ValidBody_Response201() throws Exception {
            final String uri = API_PREFIX + "/people";

            PersonDtoInput expectedPerson = new PersonDtoInput("Yuriy Nikulin",
                    LocalDate.of(1921, 12, 18));
            String requestBody = objectMapper.writeValueAsString(expectedPerson);

            mockMvc.perform(post(uri).content(requestBody)).andExpectAll(
                    status().isCreated(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(6),
                    jsonPath("$.name").value(expectedPerson.name()),
                    jsonPath("$.['date of birth']").value(expectedPerson.dob().toString()));
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Invalid request, expect 400")
        public void PostPeopleURI_InvalidBody_Response400() throws Exception {
            final String uri = API_PREFIX + "/people";

            mockMvc.perform(post(uri).content("{\"first name\": \"Yuriy\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void PostPeopleURI_Unauthorized_Response403() throws Exception {
            final String uri = API_PREFIX + "/people";

            PersonDtoInput person = new PersonDtoInput("Yuriy Nikulin",
                    LocalDate.of(1921, 12, 18));
            String requestBody = objectMapper.writeValueAsString(person);

            mockMvc.perform(post(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT")
    class Put {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, valid request, expect 200")
        public void PutPeopleURI_ValidRequest_Response200() throws Exception {
            long expectedPersonId = 2L;
            String uri = API_PREFIX + "/people/" + expectedPersonId;
            String expectedName = "Nonna Mordyukova";
            String expectedBirthday = "1925-11-25";
            String requestBody = objectMapper.createObjectNode()
                    .put("id", expectedPersonId + 1)  //expected to be ignored
                    .put("name", expectedName)
                    .put("dob", expectedBirthday)
                    .toString();

            mockMvc.perform(put(uri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(expectedPersonId),
                    jsonPath("$.name").value(expectedName),
                    jsonPath("$.['date of birth']").value(expectedBirthday));
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, invalid request, expect 400")
        public void PutPeopleURI_InvalidRequest_Response400() throws Exception {
            long personId = 2L;
            String uri = API_PREFIX + "/people/" + personId;
            String requestBody = objectMapper.createObjectNode()
                    .put("id", personId + 1)
                    .put("not_a_field", "random_data")
                    .toString();

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing id, valid request, expect 404")
        public void PutPeopleURI_NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(put(uri).content("{\"name\": \"Bogdan\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void PutPeopleURI_Unauthorized_Response403() throws Exception {
            long personId = 2L;
            String uri = API_PREFIX + "/people/" + personId;
            String name = "Nonna Mordyukova";
            String dob = "1925-11-25";
            String requestBody = objectMapper.createObjectNode()
                    .put("id", personId + 1)
                    .put("name", name)
                    .put("dob", dob)
                    .toString();

            mockMvc.perform(put(uri).content(requestBody))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE")
    class Delete {
        @Transactional
        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Existing id, expect 204")
        public void DeletePeopleURI_ExistingId_Response204() throws Exception {
            String uri = API_PREFIX + "/people/1";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = {ROLE_ADMIN})
        @DisplayName("Not existing id, expect 404")
        public void DeletePeopleURI_NotExistingId_Response404() throws Exception {
            String uri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(uri))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthorized, expect 403")
        public void DeletePeopleURI_Unauthorized_Response403() throws Exception {
            String uri = API_PREFIX + "/people/1";

            mockMvc.perform(delete(uri))
                    .andExpect(status().isForbidden());
        }
    }
}
