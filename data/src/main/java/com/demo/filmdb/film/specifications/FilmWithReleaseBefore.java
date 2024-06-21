package com.demo.filmdb.film.specifications;

import com.demo.filmdb.film.Film;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public class FilmWithReleaseBefore implements Specification<Film> {

    private final LocalDate releaseBefore;

    public FilmWithReleaseBefore(@Nullable LocalDate releaseBefore) {
        this.releaseBefore = releaseBefore;
    }

    @Override
    public Predicate toPredicate(Root<Film> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (releaseBefore == null) {
            return null;
        }
        return builder.lessThan(root.get("releaseDate"), releaseBefore);
    }
}
