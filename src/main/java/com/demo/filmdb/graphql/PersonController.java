package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.CreatePersonInput;
import com.demo.filmdb.graphql.inputs.DeletePersonInput;
import com.demo.filmdb.graphql.inputs.UpdatePersonInput;
import com.demo.filmdb.graphql.payloads.CreatePersonPayload;
import com.demo.filmdb.graphql.payloads.DeletePersonPayload;
import com.demo.filmdb.graphql.payloads.UpdatePersonPayload;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.graphql.enums.SortablePersonField;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller("graphqlPersonController")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @QueryMapping
    public Iterable<Person> people(
            @Argument int page,
            @Argument int pageSize,
            @Argument @Nullable SortablePersonField sortBy,
            @Argument @Nullable Sort.Direction sortDirection,
            @Argument @Nullable String name,
            @Argument @Nullable LocalDate bornAfter,
            @Argument @Nullable LocalDate bornBefore
    ) {
        String sortByField = sortBy == null ? null : sortBy.getFieldName();
        return personService.getPeople(page, pageSize, sortByField, sortDirection, name, bornAfter, bornBefore);
    }

    @MutationMapping
    public CreatePersonPayload createPerson(@Argument CreatePersonInput input) {
        final Person createdPerson = personService.createPerson(input.personInput());
        return new CreatePersonPayload(createdPerson);
    }

    @QueryMapping
    public @Nullable Person person(@Argument Long id) {
        return personService.getPerson(id).orElse(null);
    }

    @MutationMapping
    public UpdatePersonPayload updatePerson(@Argument UpdatePersonInput input) {
        final Person updatedPerson = personService.updatePerson(input.id(), input.personInput());
        return new UpdatePersonPayload(updatedPerson);
    }

    @MutationMapping
    public DeletePersonPayload deletePerson(@Argument DeletePersonInput input) {
        final Long personId = input.id();
        personService.deletePerson(personId);
        return new DeletePersonPayload(personId);
    }
}
