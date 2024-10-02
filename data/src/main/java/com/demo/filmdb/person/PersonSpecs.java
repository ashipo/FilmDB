package com.demo.filmdb.person;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class PersonSpecs {

    public Specification<Person> nameContains(@Nullable String name) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            String[] nameParts = name.trim().split("\\s+");
            List<Predicate> predicates = Arrays.stream(nameParts)
                    .map(p -> builder.like(builder.lower(root.get("name")), "%" + p.toLowerCase() + "%")).toList();
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Person> bornAfter(@Nullable LocalDate date) {
        return (root, query, builder) -> {
            if (date == null) {
                return null;
            }
            return builder.greaterThan(root.get("dateOfBirth"), date);
        };
    }

    public Specification<Person> bornBefore(@Nullable LocalDate date) {
        return (root, query, builder) -> {
            if (date == null) {
                return null;
            }
            return builder.lessThan(root.get("dateOfBirth"), date);
        };
    }
}
