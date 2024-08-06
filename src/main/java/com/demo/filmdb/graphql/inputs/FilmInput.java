package com.demo.filmdb.graphql.inputs;

import org.springframework.lang.Nullable;

import java.time.LocalDate;

public record FilmInput(String title, LocalDate releaseDate, @Nullable String synopsis) {
}
