package com.demo.filmdb.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.demo.filmdb.rest.Util.*;
import static com.demo.filmdb.rest.util.Path.API_PREFIX;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Person search")
public class PersonSearchIntegrationTests {
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

    @ParameterizedTest(name = "containing \"{0}\", expect to find {1} people")
    @MethodSource("com.demo.filmdb.rest.PersonSearchIntegrationTests#nameFilterAndCountProvider")
    @DisplayName("Filter by name")
    public void GetPeopleSearchURI_Name_Response200(String nameFilter, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/people/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("name", nameFilter)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.people.length()").value(expectedCount));
            for (int i = 0; i < expectedCount; i++) {
                actual.andExpect(jsonPath("$._embedded.people[" + i + "].name").
                        value(containsStringIgnoringCase(nameFilter)));
            }
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @ParameterizedTest(name = "\"{0}\", expect to find {1} people")
    @MethodSource("com.demo.filmdb.rest.PersonSearchIntegrationTests#bornAfterAndCountProvider")
    @DisplayName("Filter born after")
    public void GetPeopleSearchURI_BornAfter_Response200(LocalDate relativeTo, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/people/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("born_after", relativeTo.toString()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.people.length()").value(expectedCount));
            for (int i = 0; i < expectedCount; i++) {
                actual.andExpect(jsonPath("$['_embedded']['people'][" + i + "]['date of birth']").
                        value(isAfter(relativeTo)));
            }
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @ParameterizedTest(name = "\"{0}\", expect to find {1} people")
    @MethodSource("com.demo.filmdb.rest.PersonSearchIntegrationTests#bornBeforeAndCountProvider")
    @DisplayName("Filter born before")
    public void GetPeopleSearchURI_BornBefore_Response200(LocalDate relativeTo, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/people/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("born_before", relativeTo.toString()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.people.length()").value(expectedCount));
            for (int i = 0; i < expectedCount; i++) {
                actual.andExpect(jsonPath("$['_embedded']['people'][" + i + "]['date of birth']").
                        value(isBefore(relativeTo)));
            }
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @Test
    @DisplayName("Filter by name and birth date")
    public void GetPeopleSearchURI_ComplexFilter_Response200() throws Exception {
        final String expectedUri = API_PREFIX + "/people/search";

        mockMvc.perform(get(expectedUri)
                        .param("name", "RUS")
                        .param("born_after", "1970-02-03")
                        .param("born_before", "1972-02-04"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._embedded.people.length()").value(1),
                        jsonPath("$._embedded.people[0].name").value("Joe Russo"),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
    }

    private static Stream<Arguments> nameFilterAndCountProvider() {
        return Stream.of(
                Arguments.arguments("aR", 2),
                Arguments.arguments("sso", 3),
                Arguments.arguments("Sia", 0)
        );
    }

    private static Stream<Arguments> bornAfterAndCountProvider() {
        return Stream.of(
                Arguments.arguments(LocalDate.of(1960, 1, 2), 5),
                Arguments.arguments(LocalDate.of(1980, 6, 6), 1),
                Arguments.arguments(LocalDate.of(1990, 3, 15), 0)
        );
    }

    private static Stream<Arguments> bornBeforeAndCountProvider() {
        return Stream.of(
                Arguments.arguments(LocalDate.of(1960, 1, 2), 0),
                Arguments.arguments(LocalDate.of(1980, 6, 6), 4),
                Arguments.arguments(LocalDate.of(1990, 3, 15), 5)
        );
    }
}
