package com.demo.filmdb;

import com.demo.filmdb.security.dtos.LoginRequestDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static com.demo.filmdb.Utils.API_PREFIX;
import static com.demo.filmdb.Utils.configureMockMvc;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Authentication")
public class AuthenticationIntegrationTests {

    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final String loginUri = API_PREFIX + "/login";

    @BeforeAll
    static void beforeAll() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = configureMockMvc(wac, springSecurity());
    }

    @Test
    @DisplayName("Valid credentials, expect 200")
    public void LoginUri_ValidCredentials_Response200() throws Exception {
        LoginRequestDto request = new LoginRequestDto("user", "password");
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(loginUri).content(requestBody)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON_VALUE),
                jsonPath("$.jwt").exists()
        );
    }

    @Test
    @DisplayName("Invalid credentials, expect 401")
    public void LoginUri_InvalidCredentials_Response401() throws Exception {
        LoginRequestDto request = new LoginRequestDto("not existing user", "password");
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(loginUri).content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid request, expect 400")
    public void LoginUri_InvalidRequest_Response400() throws Exception {
        mockMvc.perform(post(loginUri).content("invalid request"))
                .andExpect(status().isBadRequest());
    }
}
