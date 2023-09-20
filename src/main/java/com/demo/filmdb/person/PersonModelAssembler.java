package com.demo.filmdb.person;

import com.demo.filmdb.film.FilmController;
import com.demo.filmdb.person.dtos.PersonDto;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class PersonModelAssembler implements RepresentationModelAssembler<Person, PersonDto> {

    private final PersonMapper personMapper;

    public PersonModelAssembler(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    @Override
    public PersonDto toModel(Person person) {
        PersonDto personDto = personMapper.personToPersonDto(person);
        personDto.add(
                linkTo(methodOn(PersonController.class).getPerson(person.getId())).withSelfRel(),
                linkTo(methodOn(PersonController.class).getFilmsDirected(person.getId())).withRel("films directed"),
                linkTo(methodOn(PersonController.class).getRoles(person.getId())).withRel("roles"),
                linkTo(methodOn(PersonController.class).getAllPeople(Pageable.unpaged())).withRel("people")
        );
        return personDto;
    }

    /**
     * Creates {@link CollectionModel} of {@link PersonDto} with a link to all people.
     * @param people must not be {@literal null}.
     * @return resulting {@link CollectionModel}.
     */
    @Override
    public CollectionModel<PersonDto> toCollectionModel(Iterable<? extends Person> people) {
        CollectionModel<PersonDto> result = RepresentationModelAssembler.super.toCollectionModel(people);
        result.add(linkTo(methodOn(PersonController.class).getAllPeople(Pageable.unpaged())).withSelfRel());
        return result;
    }

    /**
     * Creates {@link CollectionModel} of a film's directors. Adds a self link and a link to the film.
     * @param directors the people who directed the film.
     * @param filmId id of the film.
     * @return resulting {@link CollectionModel}.
     */
    public CollectionModel<PersonDto> directorsCollectionModel(Iterable<? extends Person> directors, Long filmId) {
        CollectionModel<PersonDto> result = RepresentationModelAssembler.super.toCollectionModel(directors);
        result.add(linkTo(methodOn(FilmController.class).getDirectors(filmId)).withSelfRel());
        result.add(linkTo(methodOn(FilmController.class).getFilm(filmId)).withRel("film"));
        return result;
    }
}
