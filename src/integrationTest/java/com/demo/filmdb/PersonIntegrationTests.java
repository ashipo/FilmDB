package com.demo.filmdb;

import com.demo.filmdb.person.dtos.PersonDtoInput;
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
public class PersonIntegrationTests {

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
        @DisplayName("GET /people, expect 200")
        public void GetPeopleURI_MockMVC_Response200() throws Exception {
            final String expectedUri = API_PREFIX + "/people";

            mockMvc.perform(get(expectedUri)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$._embedded.people.length()").value(5),
                    jsonPath("$._links.self.href").value(containsString(expectedUri)));
        }

        @Test
        @DisplayName("GET /people/{existing id}, expect 200")
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
        @DisplayName("GET /people/{not existing id}, expect 404")
        public void GetPeopleURI_NotExistingId_Response404() throws Exception {
            final String expectedUri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(get(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Post {
        @Transactional
        @Test
        @DisplayName("POST /people valid request, expect 201")
        public void PostPeopleURI_ValidBody_Response201() throws Exception {
            final String expectedUri = API_PREFIX + "/people";

            PersonDtoInput expectedPerson = new PersonDtoInput("Yuriy Nikulin",
                    LocalDate.of(1921, 12, 18));
            String requestBody = objectMapper.writeValueAsString(expectedPerson);

            mockMvc.perform(post(expectedUri).content(requestBody)).andExpectAll(
                    status().isCreated(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(6),
                    jsonPath("$.name").value(expectedPerson.name()),
                    jsonPath("$.['date of birth']").value(expectedPerson.dob().toString()));
        }

        @Test
        @DisplayName("POST /people invalid request, expect 400")
        public void PostPeopleURI_InvalidBody_Response400() throws Exception {
            final String expectedUri = API_PREFIX + "/people";

            mockMvc.perform(post(expectedUri).content("{\"first name\": \"Yuriy\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Put {
        @Transactional
        @Test
        @DisplayName("PUT /people/{existing id} valid request, expect 200")
        public void PutPeopleURI_ValidRequest_Response200() throws Exception {
            long expectedPersonId = 2L;
            String expectedUri = API_PREFIX + "/people/" + expectedPersonId;
            String expectedName = "Nonna Mordyukova";
            String expectedBirthday = "1925-11-25";
            String requestBody = objectMapper.createObjectNode()
                    .put("id", expectedPersonId + 1)  //expected to be ignored
                    .put("name", expectedName)
                    .put("dob", expectedBirthday)
                    .toString();

            mockMvc.perform(put(expectedUri).content(requestBody)).andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.id").value(expectedPersonId),
                    jsonPath("$.name").value(expectedName),
                    jsonPath("$.['date of birth']").value(expectedBirthday));
        }

        @Test
        @DisplayName("PUT /people/{existing id} invalid request, expect 400")
        public void PutPeopleURI_InvalidRequest_Response400() throws Exception {
            long expectedPersonId = 2L;
            String expectedUri = API_PREFIX + "/people/" + expectedPersonId;
            String requestBody = objectMapper.createObjectNode()
                    .put("id", expectedPersonId + 1)  //expected to be ignored
                    .put("not_a_field", "random_data")
                    .toString();

            mockMvc.perform(put(expectedUri).content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /people/{not existing id} valid request, expect 404")
        public void PutPeopleURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(put(expectedUri).content("{\"name\": \"Bogdan\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Delete {
        @Transactional
        @Test
        @DisplayName("DELETE /people/{existing id}, expect 204")
        public void DeletePeopleURI_ExistingId_Response204() throws Exception {
            String expectedUri = API_PREFIX + "/people/1";

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /people/{not existing id}, expect 404")
        public void DeletePeopleURI_NotExistingId_Response404() throws Exception {
            String expectedUri = API_PREFIX + "/people/" + NOT_EXISTING_ID;

            mockMvc.perform(delete(expectedUri))
                    .andExpect(status().isNotFound());
        }
    }
}
