package com.demo.filmdb.film.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An input DTO for a film
 */
public record FilmDtoInput(
        @NotBlank String title,
        @NotNull LocalDate releaseDate,
        @Nullable String synopsis
) implements Serializable {
}
