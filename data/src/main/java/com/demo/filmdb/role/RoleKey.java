package com.demo.filmdb.role;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoleKey implements Serializable {
    @Column(name = "film_id")
    private Long filmId;
    @Column(name = "person_id")
    private Long personId;

    public RoleKey() {
    }

    public RoleKey(Long filmId, Long personId) {
        this.filmId = filmId;
        this.personId = personId;
    }

    public Long getFilmId() {
        return filmId;
    }

    public void setFilmId(Long filmId) {
        this.filmId = filmId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleKey roleKey = (RoleKey) o;
        return filmId.equals(roleKey.filmId) && personId.equals(roleKey.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filmId, personId);
    }

    @Override
    public String toString() {
        return "RoleKey{" +
                "filmId=" + filmId +
                ", personId=" + personId +
                '}';
    }
}
