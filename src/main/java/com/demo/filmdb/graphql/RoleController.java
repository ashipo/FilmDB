package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.CrewMemberId;
import com.demo.filmdb.graphql.inputs.DeleteRoleInput;
import com.demo.filmdb.graphql.inputs.RoleInput;
import com.demo.filmdb.graphql.inputs.UpdateCastInput;
import com.demo.filmdb.graphql.payloads.DeleteRolePayload;
import com.demo.filmdb.graphql.payloads.UpdateCastPayload;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;

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
    public @Nullable Role role(@Argument CrewMemberId id) {
        return roleService.getRole(id.filmId(), id.personId());
    }

    @MutationMapping
    public Role updateRole(@Argument RoleInput roleInput) {
        return roleService.updateRole(roleInput.id().filmId(), roleInput.id().personId(), roleInput.character());
    }

    @MutationMapping
    public DeleteRolePayload deleteRole(@Argument DeleteRoleInput input) {
        Long filmId = input.id().filmId();
        Long personId = input.id().personId();
        roleService.deleteRole(filmId, personId);
        return new DeleteRolePayload(filmId, personId);
    }

    @MutationMapping
    public UpdateCastPayload updateCast(@Argument UpdateCastInput input) {
        List<Role> updatedCast = roleService.updateCast(input.filmId(), input.cast());
        return new UpdateCastPayload(updatedCast);
    }
}
