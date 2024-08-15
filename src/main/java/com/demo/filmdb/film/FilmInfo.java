package com.demo.filmdb.film;

import org.springframework.lang.Nullable;

import java.time.LocalDate;

public interface FilmInfo {

    String getTitle();

    LocalDate getReleaseDate();

    @Nullable
    String getSynopsis();
}
