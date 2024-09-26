package com.demo.filmdb.person;

import org.springframework.lang.Nullable;

import java.time.LocalDate;

public interface PersonInfo {

    String getName();

    @Nullable
    LocalDate getDateOfBirth();
}
