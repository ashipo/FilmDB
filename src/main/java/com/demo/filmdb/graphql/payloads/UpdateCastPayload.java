package com.demo.filmdb.graphql.payloads;

import com.demo.filmdb.role.Role;

import java.util.List;

public record UpdateCastPayload(List<Role> cast) {
}
