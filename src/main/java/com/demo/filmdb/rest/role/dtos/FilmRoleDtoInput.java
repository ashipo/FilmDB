package com.demo.filmdb.rest.role.dtos;

import com.demo.filmdb.role.CastMember;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * An input DTO for a cast member of a film
 */
public record FilmRoleDtoInput(@NotNull Long personId, String character) implements Serializable, CastMember {

    @Override
    public Long getPersonId() {
        return personId;
    }

    @Override
    public String getCharacter() {
        return character;
    }
}
