package com.demo.filmdb.graphql.inputs;

public record UpdatePersonInput(Long personId, PersonInput person) {
}
