package com.demo.filmdb.utils;

import com.demo.filmdb.film.FilmController;
import com.demo.filmdb.person.PersonController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.demo.filmdb.utils.Path.API_PREFIX;
import static com.demo.filmdb.utils.SpringDocConfig.SUCCESS;
import static com.demo.filmdb.utils.SpringDocConfig.TAG_ROOT;
import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = API_PREFIX, produces = APPLICATION_JSON_VALUE)
public class RootController {

    @Operation(tags = TAG_ROOT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
    })
    @GetMapping
    public Map<String, String> getRoot() {
        Map<String, String> resources = new HashMap<>();
        String filmsHref = linkTo(methodOn(FilmController.class).getAllFilms(Pageable.unpaged())).toString();
        resources.put("films_url", filmsHref);
        String peopleHref = linkTo(methodOn(PersonController.class).getAllPeople(Pageable.unpaged())).toString();
        resources.put("people_url", peopleHref);
        return resources;
    }
}
