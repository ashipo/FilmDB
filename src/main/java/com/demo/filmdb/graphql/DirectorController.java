package com.demo.filmdb.graphql;

import com.demo.filmdb.director.DirectorService;
import com.demo.filmdb.graphql.inputs.SetDirectorInput;
import com.demo.filmdb.graphql.payloads.SetDirectorPayload;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DirectorController {

    private final DirectorService directorService;

    public DirectorController(
            DirectorService directorService
    ) {
        this.directorService = directorService;
    }

    @MutationMapping
    public SetDirectorPayload setDirector(@Argument SetDirectorInput input) {
        Long filmId = input.id().filmId();
        Long personId = input.id().personId();
        directorService.setDirector(filmId, personId);
        return new SetDirectorPayload(filmId, personId);
    }
}
