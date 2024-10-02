package com.demo.filmdb.film;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class FilmSpecs {

    public Specification<Film> titleContains(@Nullable String title) {
        return (root, query, builder) -> {
            if (title == null || title.isBlank()) {
                return null;
            }
            String[] titleWords = title.trim().split("\\s+");
            List<Predicate> predicates = Arrays.stream(titleWords)
                    .map(w -> builder.like(builder.lower(root.get("title")), "%" + w.toLowerCase() + "%")).toList();
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Film> releaseBefore(@Nullable LocalDate date) {
        return (root, query, builder) -> {
            if (date == null) {
                return null;
            }
            return builder.lessThan(root.get("releaseDate"), date);
        };
    }

    public Specification<Film> releaseAfter(@Nullable LocalDate date) {
        return (root, query, builder) -> {
            if (date == null) {
                return null;
            }
            return builder.greaterThan(root.get("releaseDate"), date);
        };
    }
}
