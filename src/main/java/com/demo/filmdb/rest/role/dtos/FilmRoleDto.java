package com.demo.filmdb.rest.role.dtos;

import com.demo.filmdb.role.Role;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link Role} entity.
 * Intended use: response roles for a film.
 */
@Relation(collectionRelation = "cast", itemRelation = "actor")
public class FilmRoleDto extends RepresentationModel<FilmRoleDto> implements Serializable {
    private final String actor;
    private final String character;

    public FilmRoleDto(String actor, String character) {
        this.actor = actor;
        this.character = character;
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
        FilmRoleDto entity = (FilmRoleDto) o;
        return Objects.equals(this.actor, entity.actor) &&
                Objects.equals(this.character, entity.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actor, character);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "actor = " + actor + ", " +
                "character = " + character + ")";
    }
}