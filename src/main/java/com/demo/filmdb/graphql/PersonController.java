package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.CreatePersonInput;
import com.demo.filmdb.graphql.payloads.CreatePersonPayload;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller("graphqlPersonController")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @MutationMapping
    public CreatePersonPayload createPerson(@Argument CreatePersonInput input) {
        final Person createdPerson = personService.createPerson(input.person());
        return new CreatePersonPayload(createdPerson);
    }
}
