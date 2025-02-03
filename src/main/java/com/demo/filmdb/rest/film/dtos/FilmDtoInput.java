package com.demo.filmdb.rest.film.dtos;

import com.demo.filmdb.film.FilmInfo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An input DTO used to create/update a film
 */
public record FilmDtoInput(
        String title,
        LocalDate releaseDate,
        String synopsis
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
