package com.demo.filmdb.film;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public interface FilmInfo {

    String getTitle();

    LocalDate getReleaseDate();

    @Nullable
    String getSynopsis();
}
