package com.demo.filmdb.person;

import com.demo.filmdb.annotations.Sortable;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.role.Role;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Sortable
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Sortable
    private String name;

    @Column(name = "date_of_birth")
    @Nullable
    @Sortable
    private LocalDate dateOfBirth;

    @ManyToMany(mappedBy = "directors")
    private final Set<Film> filmsDirected = new LinkedHashSet<>();

    @OneToMany(mappedBy = "person")
    private final Set<Role> roles = new LinkedHashSet<>();

    public Person() {
    }

    public Person(String name, @Nullable LocalDate dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Do not use this directly!
     * To add Person-directed-Film relation must use Film.addDirector() instead.
     *
     * @param film to add
     */
    public void addFilmDirected(Film film) {
        filmsDirected.add(film);
    }

    public Set<Film> getFilmsDirected() {
        return Collections.unmodifiableSet(filmsDirected);
    }

    /**
     * Do not use this directly!
     * To remove Person-directed-Film relation must use Film.removeDirector() instead.
     *
     * @param film to remove
     */
    public void removeFilmDirected(Film film) {
        filmsDirected.remove(film);
    }

    public void removeFilmsDirected() {
        for (Film film : filmsDirected) {
            film.removeDirector(this);
        }
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Nullable
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@Nullable LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date of birth=" + dateOfBirth +
                '}';
    }
}
