package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.CrewMemberId;
import com.demo.filmdb.graphql.inputs.RoleInput;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller("graphqlRoleController")
public class RoleController {

    private final RoleService roleService;

    public RoleController(
            RoleService roleService
    ) {
        this.roleService = roleService;
    }

    @MutationMapping
    public Role createRole(@Argument RoleInput roleInput) {
        return roleService.createRole(roleInput.id().filmId(), roleInput.id().personId(), roleInput.character());
    }

    @QueryMapping
    public Role role(@Argument CrewMemberId id) {
        return roleService.getRole(id.filmId(), id.personId());
    }

    @MutationMapping
    public Role updateRole(@Argument RoleInput roleInput) {
        return roleService.updateRole(roleInput.id().filmId(), roleInput.id().personId(), roleInput.character());
    }
}
