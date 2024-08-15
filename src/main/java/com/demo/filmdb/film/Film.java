package com.demo.filmdb.film;

import com.demo.filmdb.annotations.Sortable;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "film")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Sortable
    private Long id;

    @Column(name = "title", nullable = false)
    @NotBlank
    @Sortable
    private String title;

    @Column(name = "release_date", nullable = false)
    @Sortable
    private LocalDate releaseDate;

    @Column(name = "synopsis")
    @Nullable
    private String synopsis;

    @ManyToMany
    @JoinTable(name = "film_person_directed",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    private final Set<Person> directors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "film")
    private final Set<Role> cast = new LinkedHashSet<>();

    public Film() {
    }

    public Film(String title, LocalDate releaseDate, String synopsis) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
    }

    public Set<Person> getDirectors() {
        return directors;
    }

    public Set<Role> getCast() {
        return cast;
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

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Updates directors for this film for the both sides of the association
     */
    public void setDirectors(Collection<Person> newDirectors) {
        removeDirectors();
        for (Person director : newDirectors) {
            director.getFilmsDirected().add(this);
        }
        directors.addAll(newDirectors);
    }

    /**
     * Add director for this film for the both sides of the association
     */
    public void addDirector(Person director) {
        directors.add(director);
        director.getFilmsDirected().add(this);
    }

    /**
     * Remove director from this film for the both sides of the association
     */
    public void removeDirector(Person director) {
        directors.remove(director);
        director.getFilmsDirected().remove(this);
    }

    /**
     * Removes directors from this film for the both sides of the association
     */
    public void removeDirectors() {
        for (Person director : directors) {
            director.getFilmsDirected().remove(this);
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
        if (!(o instanceof Film film)) return false;
        return Objects.equals(title, film.title) && Objects.equals(releaseDate, film.releaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, releaseDate);
    }
}
