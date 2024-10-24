package com.demo.filmdb.graphql;

import com.demo.filmdb.director.DirectorService;
import com.demo.filmdb.graphql.inputs.DeleteDirectorInput;
import com.demo.filmdb.graphql.inputs.SetDirectorInput;
import com.demo.filmdb.graphql.inputs.UpdateDirectorsInput;
import com.demo.filmdb.graphql.payloads.DeleteDirectorPayload;
import com.demo.filmdb.graphql.payloads.SetDirectorPayload;
import com.demo.filmdb.graphql.payloads.UpdateDirectorsPayload;
import com.demo.filmdb.person.Person;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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

    @MutationMapping
    public DeleteDirectorPayload deleteDirector(@Argument DeleteDirectorInput input) {
        Long filmId = input.id().filmId();
        Long personId = input.id().personId();
        directorService.deleteDirector(filmId, personId);
        return new DeleteDirectorPayload(filmId, personId);
    }

    @MutationMapping
    public UpdateDirectorsPayload updateDirectors(@Argument UpdateDirectorsInput input) {
        List<Person> directors = directorService.updateDirectors(input.filmId(), input.directorsIds());
        return new UpdateDirectorsPayload(directors);
    }
}
