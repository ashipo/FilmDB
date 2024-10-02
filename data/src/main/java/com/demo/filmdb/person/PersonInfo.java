package com.demo.filmdb.person;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public interface PersonInfo {

    String getName();

    @Nullable
    LocalDate getDateOfBirth();
}
