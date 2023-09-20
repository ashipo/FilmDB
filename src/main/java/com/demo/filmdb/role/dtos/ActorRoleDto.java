package com.demo.filmdb.role.dtos;

import com.demo.filmdb.role.Role;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link Role} entity.
 * Intended use: response roles for a person.
 */
@Relation(collectionRelation = "roles", itemRelation = "role")
public class ActorRoleDto extends RepresentationModel<ActorRoleDto> implements Serializable {
    private final String film;
    private final String character;

    public ActorRoleDto(String film, String character) {
        this.film = film;
        this.character = character;
    }

    public String getFilm() {
        return film;
    }

    public String getCharacter() {
        return character;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActorRoleDto entity = (ActorRoleDto) o;
        return Objects.equals(this.film, entity.film) &&
                Objects.equals(this.character, entity.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(film, character);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "film = " + film + ", " +
                "character = " + character + ")";
    }
}