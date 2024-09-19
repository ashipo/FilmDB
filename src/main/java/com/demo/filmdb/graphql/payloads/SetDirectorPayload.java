package com.demo.filmdb.graphql.payloads;

public record SetDirectorPayload(Long filmId, Long personId) {
}
