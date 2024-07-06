package com.demo.filmdb.graphql.inputs;

import java.time.LocalDate;

public record FilmInput(String title, LocalDate releaseDate, String synopsis) {
}
