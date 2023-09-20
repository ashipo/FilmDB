package com.demo.filmdb.person.specifications;

import com.demo.filmdb.person.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public class PersonBornBefore implements Specification<Person> {

    private final LocalDate bornBefore;

    public PersonBornBefore(@Nullable LocalDate bornBefore) {
        this.bornBefore = bornBefore;
    }

    @Override
    public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (bornBefore == null) {
            return null;
        }
        return builder.lessThan(root.get("dob"), bornBefore);
    }
}
