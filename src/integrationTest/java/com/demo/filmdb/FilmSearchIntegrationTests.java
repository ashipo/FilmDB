package com.demo.filmdb;

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

import static com.demo.filmdb.Utils.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class FilmSearchIntegrationTests {

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

    @ParameterizedTest(name = "containing \"{0}\", expect to find {1} films")
    @MethodSource("com.demo.filmdb.FilmSearchIntegrationTests#titleFilterAndCountProvider")
    @DisplayName("Filter by title")
    public void GetFilmsSearchURI_Title_Response200(String titleFilter, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/films/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("title", titleFilter)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.films.length()").value(expectedCount));
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
        for (int i = 0; i < expectedCount; i++) {
            actual.andExpect(jsonPath("$._embedded.films[" + i + "].title").
                    value(containsStringIgnoringCase(titleFilter)));
        }
    }

    @ParameterizedTest(name = "\"{0}\", expect to find {1} films")
    @MethodSource("com.demo.filmdb.FilmSearchIntegrationTests#releaseAfterAndCountProvider")
    @DisplayName("Filter by release date after")
    public void GetFilmsSearchURI_ReleaseAfter_Response200(LocalDate relativeTo, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/films/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("release_after", relativeTo.toString()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.films.length()").value(expectedCount));
            for (int i = 0; i < expectedCount; i++) {
                actual.andExpect(jsonPath("$['_embedded']['films'][" + i + "]['release date']").
                        value(isAfter(relativeTo)));
            }
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @ParameterizedTest(name = "\"{0}\", expect to find {1} films")
    @MethodSource("com.demo.filmdb.FilmSearchIntegrationTests#releaseBeforeAndCountProvider")
    @DisplayName("Filter by release date before")
    public void GetFilmsSearchURI_ReleaseBefore_Response200(LocalDate relativeTo, int expectedCount) throws Exception {
        final String expectedUri = API_PREFIX + "/films/search";

        ResultActions actual = mockMvc.perform(get(expectedUri).param("release_before", relativeTo.toString()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
        if (expectedCount > 0) {
            actual.andExpect(jsonPath("$._embedded.films.length()").value(expectedCount));
            for (int i = 0; i < expectedCount; i++) {
                actual.andExpect(jsonPath("$['_embedded']['films'][" + i + "]['release date']").
                        value(isBefore(relativeTo)));
            }
        } else {
            actual.andExpect(jsonPath("$._embedded").doesNotExist());
        }
    }

    @Test
    @DisplayName("Filter by title and release date")
    public void GetFilmsSearchURI_ComplexFilter_Response200() throws Exception {
        final String expectedUri = API_PREFIX + "/films/search";

        mockMvc.perform(get(expectedUri)
                        .param("title", "IT")
                        .param("release_after", "2016-12-30")
                        .param("release_before", "2018-12-30"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$._embedded.films.length()").value(1),
                        jsonPath("$._embedded.films[0].title").value("Avengers: Infinity War"),
                        jsonPath("$._links.self.href").value(containsString(expectedUri)));
    }

    private static Stream<Arguments> titleFilterAndCountProvider() {
        return Stream.of(
                Arguments.arguments("aR", 2),
                Arguments.arguments("ra", 2),
                Arguments.arguments("Mango", 0)
        );
    }

    private static Stream<Arguments> releaseAfterAndCountProvider() {
        return Stream.of(
                Arguments.arguments(LocalDate.of(1990, 1, 2), 3),
                Arguments.arguments(LocalDate.of(2018, 6, 6), 1),
                Arguments.arguments(LocalDate.of(2020, 3, 15), 0)
        );
    }

    private static Stream<Arguments> releaseBeforeAndCountProvider() {
        return Stream.of(
                Arguments.arguments(LocalDate.of(1990, 1, 2), 0),
                Arguments.arguments(LocalDate.of(2018, 6, 6), 2),
                Arguments.arguments(LocalDate.of(2020, 3, 15), 3)
        );
    }
}
