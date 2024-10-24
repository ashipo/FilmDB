package com.demo.filmdb.rest.film.dtos;

import com.demo.filmdb.film.FilmInfo;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An input DTO for a film
 */
public record FilmDtoInput(
        @NotBlank String title,
        @NotNull LocalDate releaseDate,
        @Nullable String synopsis
) implements Serializable, FilmInfo {
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String getSynopsis() {
        return synopsis;
    }
}
