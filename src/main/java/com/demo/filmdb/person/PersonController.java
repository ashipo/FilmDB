package com.demo.filmdb.person;

import com.demo.filmdb.annotations.ApiPrefixRestController;
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
import com.demo.filmdb.utils.SortUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

import static com.demo.filmdb.utils.SpringDocConfig.TAG_DIRECTORS;
import static com.demo.filmdb.utils.SpringDocConfig.TAG_PEOPLE;

@ApiPrefixRestController
public class PersonController {

    private final PersonService personService;
    private final PersonModelAssembler personModelAssembler;
    private final FilmModelAssembler filmModelAssembler;
    private final ActorRoleModelAssembler roleModelAssembler;
    private final PersonMapper personMapper;
    private final PagedResourcesAssembler<Person> pagedResourcesAssembler;
    public PersonController(PersonService personService,
                            PersonModelAssembler personModelAssembler,
                            FilmModelAssembler filmModelAssembler,
                            ActorRoleModelAssembler roleModelAssembler,
                            PersonMapper personMapper,
                            PagedResourcesAssembler<Person> pagedResourcesAssembler) {
        this.personService = personService;
        this.personModelAssembler = personModelAssembler;
        this.filmModelAssembler = filmModelAssembler;
        this.roleModelAssembler = roleModelAssembler;
        this.personMapper = personMapper;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }
    @Operation(summary = "Find people", tags = TAG_PEOPLE)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful operation") })
    @GetMapping(value = "/people/search", produces = "application/json")
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
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful operation") })
    @GetMapping(value = "/people", produces = "application/json")
    public CollectionModel<PersonDto> getAllPeople(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Person.class);
        Page<Person> peoplePage = personService.getAllPeople(filteredPageable);
        return pagedResourcesAssembler.toModel(peoplePage, personModelAssembler);
    }

    @Operation(summary = "Create a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person created"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content) })
    @PostMapping(value = "/people", produces = "application/json")
    public ResponseEntity<PersonDto> createPerson(@RequestBody @Valid PersonDtoInput personDtoInput){
        Person person = personMapper.personDtoInputToPerson(personDtoInput);
        PersonDto newPersonDto = personModelAssembler.toModel(personService.savePerson(person));
        return ResponseEntity.created(newPersonDto.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(newPersonDto);
    }

    @Operation(summary = "Get a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person found"),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content) })
    @GetMapping(value = "/people/{personId}", produces = "application/json")
    public PersonDto getPerson(@PathVariable Long personId) {
        Person person = personService.getPerson(personId);
        return personModelAssembler.toModel(person);
    }

    @Operation(summary = "Update a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content) })
    @PutMapping(value = "/people/{personId}", produces = "application/json")
    public PersonDto updatePerson(@PathVariable Long personId, @Valid @RequestBody PersonDtoInput personDtoInput) {
        Person personToUpdate = personService.getPerson(personId);
        Person updatedPerson = personMapper.updatePersonFromPersonDtoInput(personDtoInput, personToUpdate);
        Person savedPerson = personService.savePerson(updatedPerson);
        return personModelAssembler.toModel(savedPerson);
    }

    @Operation(summary = "Delete a person", tags = TAG_PEOPLE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Person deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content) })
    @DeleteMapping("/people/{personId}")
    public ResponseEntity<?> deletePerson(@PathVariable Long personId) {
        personService.deletePerson(personId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get films directed by a person", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content) })
    @GetMapping(value = "/people/{personId}/films_directed", produces = "application/json")
    public CollectionModel<FilmDto> getFilmsDirected(@PathVariable Long personId) {
        Collection<Film> directed = personService.getDirected(personId);
        return filmModelAssembler.directedFilmsCollectionModel(directed, personId);
    }

    @Operation(summary = "Get roles acted by a person", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content) })
    @GetMapping(value = "/people/{personId}/roles", produces = "application/json")
    public CollectionModel<ActorRoleDto> getRoles(@PathVariable Long personId) {
        Collection<Role> roles = personService.getRoles(personId);
        return roleModelAssembler.toCollectionModel(roles, personId);
    }
}
