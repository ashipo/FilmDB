package com.demo.filmdb.film.dtos;

import com.demo.filmdb.film.Film;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link Film} entity.
 * Intended use: request.
 */
public record FilmDtoInput(@NotEmpty String title, @NotNull LocalDate releaseDate, String synopsis) implements Serializable {
}
