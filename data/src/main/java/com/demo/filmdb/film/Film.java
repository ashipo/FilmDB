package com.demo.filmdb.film;

import com.demo.filmdb.annotations.Sortable;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Entity
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Sortable
    private Long id;

    @NotBlank
    @Sortable
    private String title;

    @NotNull
    @Sortable
    private LocalDate releaseDate;

    @Nullable
    private String synopsis;

    @ManyToMany
    @JoinTable(name = "film_person_directed",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    private final Set<Person> directors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "film")
    private final Set<Role> cast = new LinkedHashSet<>();

    @SuppressWarnings("unused")
    Film() {
    }

    public Film(String title, LocalDate releaseDate, @Nullable String synopsis) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
    }

    public Film(Long id, String title, LocalDate releaseDate, @Nullable String synopsis) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
    }

    public Set<Role> getCast() {
        return Collections.unmodifiableSet(cast);
    }

    /**
     * Adds a Role to Film's side of association
     *
     * @param role to add
     */
    public void addRole(Role role) {
        requireNonNull(role, "Can't add null Role");
        if (!role.getFilm().equals(this)) {
            throw new IllegalArgumentException("Can't add Role that belongs to another Film");
        }
        cast.add(role);
    }

    @Nullable
    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(@Nullable String synopsis) {
        this.synopsis = synopsis;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    /**
     * Sets a person as a director of this film for the both sides of the association
     *
     * @param director person to make a director
     */
    public void addDirector(Person director) {
        directors.add(director);
        director.addFilmDirected(this);
    }

    public Set<Person> getDirectors() {
        return Collections.unmodifiableSet(directors);
    }

    /**
     * Sets a collection of people as the directors of this film for the both sides of the association
     *
     * @param newDirectors collection of the new directors
     */
    public void setDirectors(Collection<Person> newDirectors) {
        removeDirectors();
        for (Person director : newDirectors) {
            director.addFilmDirected(this);
        }
        directors.addAll(newDirectors);
    }

    /**
     * Remove a person from the directors of this film for the both sides of the association
     *
     * @param director person to remove
     */
    public void removeDirector(Person director) {
        directors.remove(director);
        director.removeFilmDirected(this);
    }

    /**
     * Removes all people from the directors of this film for the both sides of the association
     */
    public void removeDirectors() {
        for (Person director : directors) {
            director.removeFilmDirected(this);
        }
        directors.clear();
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", release date=" + releaseDate +
                ", synopsis='" + synopsis + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Film other)) return false;
        return id != null
                && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
