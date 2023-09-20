package com.demo.filmdb.person;

import com.demo.filmdb.annotations.Sortable;
import com.demo.filmdb.film.Film;
import com.demo.filmdb.role.Role;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
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
    @Sortable
    private String name;

    @Column(name = "dob")
    @Sortable
    private LocalDate dob;

    @ManyToMany(mappedBy = "directors")
    private Set<Film> filmsDirected = new LinkedHashSet<>();

    @OneToMany(mappedBy = "person")
    private Set<Role> roles = new LinkedHashSet<>();

    public Person() {
    }

    public Person(String name, LocalDate dob) {
        this.name = name;
        this.dob = dob;
    }

    public Set<Film> getFilmsDirected() {
        return filmsDirected;
    }

    public void setFilmsDirected(Set<Film> filmsDirected) {
        this.filmsDirected = filmsDirected;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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
                ", date of birth=" + dob +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Person person = (Person) o;
        return id != null && Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
