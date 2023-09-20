package com.demo.filmdb.person.specifications;

import com.demo.filmdb.person.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

public class PersonWithName implements Specification<Person> {

    private final String name;

    public PersonWithName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String[] nameParts = name.trim().split("\\s+");
        List<Predicate> predicates = Arrays.stream(nameParts).
                map(p -> builder.like(builder.lower(root.get("name")), "%"+p.toLowerCase()+"%")).toList();
        return builder.or(predicates.toArray(new Predicate[0]));
    }
}
