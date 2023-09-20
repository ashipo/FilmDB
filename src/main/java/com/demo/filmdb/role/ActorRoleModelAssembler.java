package com.demo.filmdb.role;

import com.demo.filmdb.film.FilmController;
import com.demo.filmdb.person.PersonController;
import com.demo.filmdb.role.dtos.ActorRoleDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ActorRoleModelAssembler implements RepresentationModelAssembler<Role, ActorRoleDto> {

    private final RoleMapper mapper;

    public ActorRoleModelAssembler(RoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ActorRoleDto toModel(Role role) {
        ActorRoleDto dtoModel = mapper.roleToActorRoleDto(role);
        final Long filmId = role.getFilm().getId();
        final Long personId = role.getPerson().getId();
        dtoModel.add(linkTo(methodOn(FilmController.class).getRole(filmId, personId)).withSelfRel());
        dtoModel.add(linkTo(methodOn(FilmController.class).getFilm(role.getFilm().getId())).withRel("film"));
        return dtoModel;
    }

    public CollectionModel<ActorRoleDto> toCollectionModel(Iterable<? extends Role> entities, Long actorId) {
        CollectionModel<ActorRoleDto> result = RepresentationModelAssembler.super.toCollectionModel(entities);
        result.add(linkTo(methodOn(PersonController.class).getRoles(actorId)).withSelfRel());
        result.add(linkTo(methodOn(PersonController.class).getPerson(actorId)).withRel("actor"));
        return result;
    }
}
