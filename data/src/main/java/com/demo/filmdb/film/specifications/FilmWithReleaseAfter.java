package com.demo.filmdb.film.specifications;

import com.demo.filmdb.film.Film;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public class FilmWithReleaseAfter implements Specification<Film> {

    private final LocalDate releaseAfter;

    public FilmWithReleaseAfter(@Nullable LocalDate releaseAfter) {
        this.releaseAfter = releaseAfter;
    }

    @Override
    public Predicate toPredicate(Root<Film> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (releaseAfter == null) {
            return null;
        }
        return builder.greaterThan(root.get("releaseDate"), releaseAfter);
    }
}
