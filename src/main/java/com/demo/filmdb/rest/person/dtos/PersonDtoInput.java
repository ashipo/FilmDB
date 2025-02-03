package com.demo.filmdb.rest.person.dtos;

import com.demo.filmdb.person.PersonInfo;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An input DTO used to create/update a person
 */
public record PersonDtoInput(
        String name,
        LocalDate dateOfBirth
) implements Serializable, PersonInfo {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
