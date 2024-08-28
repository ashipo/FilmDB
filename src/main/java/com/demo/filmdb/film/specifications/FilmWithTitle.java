package com.demo.filmdb.film.specifications;

import com.demo.filmdb.film.Film;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

public class FilmWithTitle implements Specification<Film> {

    private final String title;

    public FilmWithTitle(@Nullable String title) {
        this.title = title;
    }

    @Override
    public Predicate toPredicate(Root<Film> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (title == null || title.isBlank()) {
            return null;
        }
        String[] titleWords = title.trim().split("\\s+");
        List<Predicate> predicates = Arrays.stream(titleWords)
                .map(w -> builder.like(builder.lower(root.get("title")), "%" + w.toLowerCase() + "%")).toList();
        return builder.or(predicates.toArray(new Predicate[0]));
    }
}
