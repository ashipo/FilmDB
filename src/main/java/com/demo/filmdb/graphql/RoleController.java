package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.inputs.*;
import com.demo.filmdb.graphql.payloads.CreateRolePayload;
import com.demo.filmdb.graphql.payloads.DeleteRolePayload;
import com.demo.filmdb.graphql.payloads.UpdateCastPayload;
import com.demo.filmdb.graphql.payloads.UpdateRolePayload;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleService;
import jakarta.annotation.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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
    public CreateRolePayload createRole(@Argument CreateRoleInput input) {
        final Role createdRole = roleService.createRole(input.id().filmId(), input.id().personId(), input.character());
        return new CreateRolePayload(createdRole);
    }

    @QueryMapping
    public @Nullable Role role(@Argument CrewMemberId id) {
        return roleService.getRole(id.filmId(), id.personId()).orElse(null);
    }

    @MutationMapping
    public UpdateRolePayload updateRole(@Argument UpdateRoleInput input) {
        final Role updatedRole = roleService.updateRole(input.id().filmId(), input.id().personId(), input.character());
        return new UpdateRolePayload(updatedRole);
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
