package com.demo.filmdb.graphql.inputs;

import com.demo.filmdb.person.PersonInfo;

import java.time.LocalDate;

public record PersonInput(String name, LocalDate dateOfBirth) implements PersonInfo {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
