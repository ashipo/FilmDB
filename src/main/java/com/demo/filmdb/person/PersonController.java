package com.demo.filmdb.person;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmModelAssembler;
import com.demo.filmdb.film.dtos.FilmDto;
import com.demo.filmdb.person.dtos.PersonDto;
import com.demo.filmdb.person.dtos.PersonDtoInput;
import com.demo.filmdb.person.specifications.PersonBornAfter;
import com.demo.filmdb.person.specifications.PersonBornBefore;
import com.demo.filmdb.person.specifications.PersonWithName;
import com.demo.filmdb.role.ActorRoleModelAssembler;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.dtos.ActorRoleDto;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.utils.SortUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collection;

import static com.demo.filmdb.util.ErrorUtil.personNotFoundMessage;
import static com.demo.filmdb.utils.Path.API_PREFIX;
import static com.demo.filmdb.utils.Path.PEOPLE;
import static com.demo.filmdb.utils.SpringDocConfig.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = API_PREFIX + PEOPLE, produces = APPLICATION_JSON_VALUE)
public class PersonController {

    private final PersonService personService;
    private final PersonModelAssembler personModelAssembler;
    private final FilmModelAssembler filmModelAssembler;
    private final ActorRoleModelAssembler roleModelAssembler;
    private final PagedResourcesAssembler<Person> pagedResourcesAssembler;

    public PersonController(PersonService personService,
                            PersonModelAssembler personModelAssembler,
                            FilmModelAssembler filmModelAssembler,
                            ActorRoleModelAssembler roleModelAssembler,
                            PagedResourcesAssembler<Person> pagedResourcesAssembler) {
        this.personService = personService;
        this.personModelAssembler = personModelAssembler;
        this.filmModelAssembler = filmModelAssembler;
        this.roleModelAssembler = roleModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Find people", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
    })
    @SecurityRequirements
    @GetMapping("/search")
    public CollectionModel<PersonDto> findPeople(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "born_after", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bornAfter,
            @RequestParam(value = "born_before", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bornBefore,
            Pageable pageable) {
        Specification<Person> spec = Specification.where(new PersonWithName(name)).
                and(new PersonBornBefore(bornBefore)).
                and(new PersonBornAfter(bornAfter));
        Pageable filteredPageable = SortUtil.filterSort(pageable, Person.class);
        Page<Person> peopleFound = personService.search(spec, filteredPageable);
        return pagedResourcesAssembler.toModel(peopleFound, personModelAssembler);
    }

    @Operation(summary = "List all people", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
    })
    @SecurityRequirements
    @GetMapping
    public CollectionModel<PersonDto> getAllPeople(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Person.class);
        Page<Person> peoplePage = personService.getAllPeople(filteredPageable);
        return pagedResourcesAssembler.toModel(peoplePage, personModelAssembler);
    }

    @Operation(summary = "Create a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "create a person", content = @Content),
    })
    @PostMapping
    public ResponseEntity<PersonDto> createPerson(@RequestBody @Valid PersonDtoInput personDtoInput) {
        Person createdPerson = personService.createPerson(personDtoInput);
        PersonDto newPersonDto = personModelAssembler.toModel(createdPerson);
        return ResponseEntity.created(newPersonDto.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(newPersonDto);
    }

    @Operation(summary = "Get a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person found"),
            @ApiResponse(responseCode = "404", description = PERSON_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{personId}")
    public PersonDto getPerson(@PathVariable Long personId) {
        Person person = personService.getPerson(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, personNotFoundMessage(personId))
        );
        return personModelAssembler.toModel(person);
    }

    @Operation(summary = "Update a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person updated"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "update a person", content = @Content),
            @ApiResponse(responseCode = "404", description = PERSON_NOT_FOUND, content = @Content),
    })
    @PutMapping("/{personId}")
    public PersonDto updatePerson(@PathVariable Long personId, @Valid @RequestBody PersonDtoInput personDtoInput) {
        try {
            Person updatedPerson = personService.updatePerson(personId, personDtoInput);
            return personModelAssembler.toModel(updatedPerson);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Delete a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Person deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "delete a person", content = @Content),
            @ApiResponse(responseCode = "404", description = PERSON_NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/{personId}")
    public ResponseEntity<?> deletePerson(@PathVariable Long personId) {
        Person person = personService.getPerson(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, personNotFoundMessage(personId))
        );
        personService.deletePerson(person);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get films directed by a person", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
            @ApiResponse(responseCode = "404", description = PERSON_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{personId}/films_directed")
    public CollectionModel<FilmDto> getFilmsDirected(@PathVariable Long personId) {
        Person person = personService.getPerson(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, personNotFoundMessage(personId))
        );
        Collection<Film> directed = person.getFilmsDirected();
        return filmModelAssembler.directedFilmsCollectionModel(directed, personId);
    }

    @Operation(summary = "Get roles acted by a person", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
            @ApiResponse(responseCode = "404", description = PERSON_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{personId}/roles")
    public CollectionModel<ActorRoleDto> getRoles(@PathVariable Long personId) {
        Person person = personService.getPerson(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, personNotFoundMessage(personId))
        );
        Collection<Role> roles = person.getRoles();
        return roleModelAssembler.toCollectionModel(roles, personId);
    }
}
