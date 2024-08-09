package com.demo.filmdb.film;

import com.demo.filmdb.annotations.Sortable;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.role.Role;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
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
    @Sortable
    private String title;

    @Column(name = "release_date", nullable = false)
    @Sortable
    private LocalDate releaseDate;

    @Column(name = "synopsis")
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

    public void setDirectors(Collection<Person> directors) {
        this.directors.clear();
        this.directors.addAll(directors);
    }

    public Set<Role> getCast() {
        return cast;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
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

    public void addDirector(Person director) {
        directors.add(director);
        director.getFilmsDirected().add(this);
    }

    public void removeDirector(Person director) {
        directors.remove(director);
        director.getFilmsDirected().remove(this);
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
}
