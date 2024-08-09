package com.demo.filmdb.role.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * An input DTO for a cast member of a film
 */
public record FilmRoleDtoInput(@NotNull Long personId, @NotBlank String character) implements Serializable, CastMember {

    @Override
    public Long getPersonId() {
        return personId;
    }

    @Override
    public String getCharacter() {
        return character;
    }
}
