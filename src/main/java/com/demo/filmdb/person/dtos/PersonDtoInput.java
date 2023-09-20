package com.demo.filmdb.person.dtos;

import com.demo.filmdb.person.Person;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * A DTO for the {@link Person} entity.
 * Intended use: request.
 */
public record PersonDtoInput(@NotEmpty String name, LocalDate dob) implements Serializable {
}
