package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Role {
    @EmbeddedId
    RoleKey id = new RoleKey();

    @ManyToOne(fetch = LAZY)
    @MapsId("filmId")
    @JoinColumn(name = "film_id")
    private Film film;

    @ManyToOne(fetch = LAZY)
    @MapsId("personId")
    @JoinColumn(name = "person_id")
    private Person person;

    @NotEmpty
    @Column(nullable = false)
    private String character;

    public Role() {
    }

    public Role(Film film, Person person, String character) {
        this.film = film;
        this.person = person;
        this.character = character;
    }

    public RoleKey getId() {
        return id;
    }

    public void setId(RoleKey id) {
        this.id = id;
    }

    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
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
