package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Role {

    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "film_id")
        private Long filmId;

        @Column(name = "person_id")
        private Long personId;

        public Id() {
        }

        public Id(Long filmId, Long personId) {
            this.filmId = filmId;
            this.personId = personId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(filmId, id.filmId) && Objects.equals(personId, id.personId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filmId, personId);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "filmId=" + filmId +
                    ", personId=" + personId +
                    '}';
        }
    }

    @EmbeddedId
    Id id = new Id();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "film_id", insertable = false, updatable = false)
    private Film film;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private Person person;

    @NotBlank
    private String character;

    public Role() {
    }

    public Role(Film film, Person person, String character) {
        this.film = film;
        this.person = person;
        this.character = character;

        this.id.filmId = film.getId();
        this.id.personId = person.getId();

        film.addRole(this);
        person.addRole(this);
    }

    public Id getId() {
        return id;
    }

    public Film getFilm() {
        return film;
    }

    public Person getPerson() {
        return person;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", film='" + film.getTitle() + '\'' +
                ", person='" + person.getName() + '\'' +
                ", character='" + character + '\'' +
                '}';
    }
}
