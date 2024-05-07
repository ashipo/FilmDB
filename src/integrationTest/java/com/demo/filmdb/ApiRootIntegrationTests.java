package com.demo.filmdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static com.demo.filmdb.Utils.API_PREFIX;
import static com.demo.filmdb.Utils.configureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("API root")
public class ApiRootIntegrationTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = configureMockMvc(wac);
    }

    @Test
    @DisplayName("GET API root, expect 200")
    public void GetApiRoot_Response200() throws Exception {
        mockMvc.perform(get(API_PREFIX)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON));
    }
}
