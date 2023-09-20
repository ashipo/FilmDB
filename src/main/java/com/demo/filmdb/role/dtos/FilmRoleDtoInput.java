package com.demo.filmdb.role.dtos;

import com.demo.filmdb.role.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * A DTO for the {@link Role} entity.
 * Intended use: request role create/bulk update for a film.
 */
public record FilmRoleDtoInput(@NotNull Long personId, @NotEmpty String character) implements Serializable {
}
