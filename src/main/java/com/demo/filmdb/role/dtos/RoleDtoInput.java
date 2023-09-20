package com.demo.filmdb.role.dtos;

import com.demo.filmdb.role.Role;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

/**
 * A DTO for the {@link Role} entity.
 * Intended use: request partial update.
 */
public record RoleDtoInput(@NotEmpty String character) implements Serializable {
}
