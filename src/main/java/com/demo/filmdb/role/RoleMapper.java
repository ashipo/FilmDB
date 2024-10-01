package com.demo.filmdb.role;

import com.demo.filmdb.role.dtos.ActorRoleDto;
import com.demo.filmdb.role.dtos.FilmRoleDto;
import com.demo.filmdb.role.dtos.RoleDto;
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
