package com.demo.filmdb.graphql.inputs;

public record RoleInput(Long filmId, Long personId, String character) {
}
