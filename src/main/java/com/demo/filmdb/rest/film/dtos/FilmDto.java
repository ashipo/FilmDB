package com.demo.filmdb.rest.film.dtos;

import com.demo.filmdb.film.Film;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link Film} entity.
 * Intended use: response.
 */
@JsonPropertyOrder({"id", "title", "synopsis", "releaseDate"})
@Relation(collectionRelation = "films", itemRelation = "film")
public class FilmDto extends RepresentationModel<FilmDto> implements Serializable {
    private final Long id;
    private final String title;
    @JsonProperty(value = "release date")
    private final LocalDate releaseDate;
    private final String synopsis;

    public FilmDto(Long id, String title, LocalDate releaseDate, String synopsis) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.synopsis = synopsis;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getSynopsis() {
        return synopsis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilmDto entity = (FilmDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.title, entity.title) &&
                Objects.equals(this.releaseDate, entity.releaseDate) &&
                Objects.equals(this.synopsis, entity.synopsis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, releaseDate, synopsis);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "title = " + title + ", " +
                "release date = " + releaseDate + ", " +
                "synopsis = " + synopsis + ")";
    }
}