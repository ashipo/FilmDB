package com.demo.filmdb.role.dtos;

import com.demo.filmdb.role.Role;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link Role} entity.
 * Intended use: response.
 */
@Relation(collectionRelation = "roles", itemRelation = "role")
public class RoleDto extends RepresentationModel<RoleDto> implements Serializable {
    private final String film;
    private final String actor;
    private final String character;

    public RoleDto(String film, String actor, String character) {
        this.film = film;
        this.actor = actor;
        this.character = character;
    }

    public String getFilm() {
        return film;
    }

    public String getActor() {
        return actor;
    }

    public String getCharacter() {
        return character;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleDto entity = (RoleDto) o;
        return Objects.equals(this.film, entity.film) &&
                Objects.equals(this.actor, entity.actor) &&
                Objects.equals(this.character, entity.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(film, actor, character);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "film = " + film + ", " +
                "actor = " + actor + ", " +
                "character = " + character + ")";
    }
}