package com.demo.filmdb.graphql.inputs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FilmInput(@NotEmpty String title, @NotNull LocalDate releaseDate, String synopsis) {
}
