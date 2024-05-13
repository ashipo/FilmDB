package com.demo.filmdb.utils;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Links;

@OpenAPIDefinition(
        info = @Info(
                title = "FilmDB API",
                version = "1.0",
                description = "FilmDB API documentation"
        ),
        tags = {@Tag(name = SpringDocConfig.TAG_ROOT),
                @Tag(name = SpringDocConfig.TAG_LOGIN),
                @Tag(name = SpringDocConfig.TAG_FILMS),
                @Tag(name = SpringDocConfig.TAG_PEOPLE),
                @Tag(name = SpringDocConfig.TAG_DIRECTORS),
                @Tag(name = SpringDocConfig.TAG_ROLES)}
)
@Configuration
public class SpringDocConfig {

    public static final String TAG_ROOT = "API Root";
    public static final String TAG_LOGIN = "Login";
    public static final String TAG_FILMS = "Films";
    public static final String TAG_PEOPLE = "People";
    public static final String TAG_DIRECTORS = "Directors";
    public static final String TAG_ROLES = "Roles";

    public static final String SUCCESS = "Successful operation";
    public static final String UNAUTHORIZED_TO = "Must have administrative rights to ";
    public static final String FILM_NOT_FOUND = "Film not found";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String PERSON_NOT_FOUND = "Person not found";

    /**
     * Hides Links object from the SpringDoc generated schemas.
     * Currently, generated example schemas for the RepresentationModel based DTOs include "_link" object
     * containing links of the RepresentationModel that don't represent real links of a DTO. Until a better solution
     * is found, "_links" are hidden altogether.
     */
    @Bean
    CommandLineRunner initSpringDoc() {
        return args -> SpringDocUtils.getConfig().addResponseTypeToIgnore(Links.class);
    }
}
