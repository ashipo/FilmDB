package com.demo.filmdb.rest.role;

import com.demo.filmdb.rest.role.dtos.ActorRoleDto;
import com.demo.filmdb.rest.role.dtos.FilmRoleDto;
import com.demo.filmdb.rest.role.dtos.RoleDto;
import com.demo.filmdb.role.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface RoleMapper {

    /* RoleDto */

    @Mapping(source = "film.title", target = "film")
    @Mapping(source = "person.name", target = "actor")
    RoleDto roleToRoleDto(Role role);

    /* ActorRoleDto */

    @Mapping(source = "film.title", target = "film")
    ActorRoleDto roleToActorRoleDto(Role role);

    /* FilmRoleDto */

    @Mapping(source = "person.name", target = "actor")
    FilmRoleDto roleToFilmRoleDto(Role role);
}
