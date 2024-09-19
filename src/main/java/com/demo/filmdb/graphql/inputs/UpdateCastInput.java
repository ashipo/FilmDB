package com.demo.filmdb.graphql.inputs;

import java.util.List;

public record UpdateCastInput(Long filmId, List<CastMemberInput> cast) {
}
