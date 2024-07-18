package com.demo.filmdb;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class Utils {
    static final String API_PREFIX = "/api";
    public static final Long NOT_EXISTING_ID = -1L;
    public static final String ROLE_ADMIN = "ADMIN";

    static MockMvc configureMockMvc(WebApplicationContext wac) {
        return commonMvcBuilder(wac)
                .build();
    }

    static MockMvc configureMockMvc(WebApplicationContext wac, MockMvcConfigurer security) {
        return commonMvcBuilder(wac)
                .apply(security)
                .build();
    }

    private static DefaultMockMvcBuilder commonMvcBuilder(WebApplicationContext wac) {
        return webAppContextSetup(wac)
                .defaultRequest(get("/")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Assert that the value is a date after given {@link LocalDate} {@code relativeTo}.
     * @param relativeTo date to compare to.
     * @return {@link Matcher}.
     */
    static Matcher<LocalDate> isAfter(LocalDate relativeTo) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Date must be after " + relativeTo);
            }

            @Override
            public boolean matches(Object actual) {
                if (actual.getClass() != String.class) {
                    throw new IllegalArgumentException();
                }
                LocalDate actualDate = LocalDate.parse((CharSequence) actual);
                return actualDate.isAfter(relativeTo);
            }
        };
    }

    /**
     * Assert that the value is a date before given {@link LocalDate}.
     * @param relativeTo date to compare to.
     * @return {@link Matcher}.
     */
    static Matcher<LocalDate> isBefore(LocalDate relativeTo) {
        return new BaseMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Date must be before " + relativeTo);
            }

            @Override
            public boolean matches(Object actual) {
                if (actual.getClass() != String.class) {
                    throw new IllegalArgumentException();
                }
                LocalDate actualDate = LocalDate.parse((CharSequence) actual);
                return actualDate.isBefore(relativeTo);
            }
        };
    }
}
