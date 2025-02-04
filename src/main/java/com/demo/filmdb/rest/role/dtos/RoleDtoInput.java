package com.demo.filmdb.rest.role.dtos;

import java.io.Serializable;

/**
 * An input DTO for a Role update
 */
public record RoleDtoInput(String character) implements Serializable {
}
