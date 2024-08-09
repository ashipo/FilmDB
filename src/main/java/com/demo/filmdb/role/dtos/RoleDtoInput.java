package com.demo.filmdb.role.dtos;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * An input DTO for a Role update
 */
public record RoleDtoInput(@NotBlank String character) implements Serializable {
}
