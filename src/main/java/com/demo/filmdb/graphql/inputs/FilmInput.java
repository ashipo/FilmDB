package com.demo.filmdb.graphql.inputs;

import com.demo.filmdb.film.FilmInfo;
import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record FilmInput(String title, LocalDate releaseDate, @Nullable String synopsis) implements FilmInfo {

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
