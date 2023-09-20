package com.demo.filmdb.person.specifications;

import com.demo.filmdb.person.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public class PersonBornAfter implements Specification<Person> {

    private final LocalDate bornAfter;

    public PersonBornAfter(@Nullable LocalDate bornAfter) {
        this.bornAfter = bornAfter;
    }

    @Override
    public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (bornAfter == null) {
            return null;
        }
        return builder.greaterThan(root.get("dob"), bornAfter);
    }
}
