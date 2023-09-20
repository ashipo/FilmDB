package com.demo.filmdb.role;

import com.demo.filmdb.role.dtos.ActorRoleDto;
import com.demo.filmdb.role.dtos.FilmRoleDto;
import com.demo.filmdb.role.dtos.RoleDto;
import com.demo.filmdb.role.dtos.RoleDtoInput;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface RoleMapper {

    /* RoleDto */

    @Mapping(source = "film.title", target = "film")
    @Mapping(source = "person.name", target = "actor")
    RoleDto roleToRoleDto(Role role);

    /* RoleDtoInput */

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Role updateRoleFromRoleDtoInput(RoleDtoInput roleDtoInput, @MappingTarget Role role);

    /* ActorRoleDto */

    @Mapping(source = "film.title", target = "film")
    ActorRoleDto roleToActorRoleDto(Role role);

    /* FilmRoleDto */

    @Mapping(source = "person.name", target = "actor")
    FilmRoleDto roleToFilmRoleDto(Role role);

    /* FilmRoleDtoInput */

}
