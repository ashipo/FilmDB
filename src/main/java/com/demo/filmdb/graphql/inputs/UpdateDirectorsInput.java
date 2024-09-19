package com.demo.filmdb.graphql.inputs;

import java.util.List;

public record UpdateDirectorsInput(Long filmId, List<Long> directorsIds) {
}
