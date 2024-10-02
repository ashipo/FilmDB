package com.demo.filmdb.rest.role;

import com.demo.filmdb.rest.film.FilmController;
import com.demo.filmdb.rest.person.PersonController;
import com.demo.filmdb.rest.role.dtos.FilmRoleDto;
import com.demo.filmdb.role.Role;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class FilmRoleModelAssembler implements RepresentationModelAssembler<Role, FilmRoleDto> {

    private final RoleMapper mapper;

    public FilmRoleModelAssembler(RoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FilmRoleDto toModel(Role role) {
        FilmRoleDto dtoModel = mapper.roleToFilmRoleDto(role);
        final Long filmId = role.getFilm().getId();
        final Long personId = role.getPerson().getId();
        dtoModel.add(linkTo(methodOn(FilmController.class).getRole(filmId, personId)).withSelfRel());
        dtoModel.add(linkTo(methodOn(PersonController.class).getPerson(personId)).withRel("actor"));
        return dtoModel;
    }

    public CollectionModel<FilmRoleDto> toCollectionModel(Iterable<? extends Role> entities, Long filmId) {
        CollectionModel<FilmRoleDto> result = RepresentationModelAssembler.super.toCollectionModel(entities);
        result.add(linkTo(methodOn(FilmController.class).getCast(filmId)).withSelfRel());
        result.add(linkTo(methodOn(FilmController.class).getFilm(filmId)).withRel("film"));
        return result;
    }

}
