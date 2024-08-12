package com.demo.filmdb.person.dtos;

import com.demo.filmdb.person.PersonInfo;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * An input DTO used to create/update a person
 */
public record PersonDtoInput(@NotBlank String name, @Nullable LocalDate dob) implements Serializable, PersonInfo {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable LocalDate getDateOfBirth() {
        return dob;
    }
}
