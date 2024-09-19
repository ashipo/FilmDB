package com.demo.filmdb.role;

import com.demo.filmdb.film.FilmController;
import com.demo.filmdb.person.PersonController;
import com.demo.filmdb.role.dtos.RoleDto;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class RoleModelAssembler implements RepresentationModelAssembler<Role, RoleDto> {

    private final RoleMapper mapper;

    public RoleModelAssembler(RoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RoleDto toModel(Role role) {
        RoleDto dtoModel = mapper.roleToRoleDto(role);
        final Long filmId = role.getFilm().getId();
        final Long personId = role.getPerson().getId();
        dtoModel.add(linkTo(methodOn(FilmController.class).getRole(filmId, personId)).withSelfRel());
        dtoModel.add(linkTo(methodOn(FilmController.class).getFilm(filmId)).withRel("film"));
        dtoModel.add(linkTo(methodOn(FilmController.class).getCast(filmId)).withRel("full cast"));
        dtoModel.add(linkTo(methodOn(PersonController.class).getPerson(personId)).withRel("actor"));
        return dtoModel;
    }
}
