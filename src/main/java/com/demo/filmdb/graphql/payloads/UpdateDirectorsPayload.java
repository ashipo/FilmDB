package com.demo.filmdb.graphql.payloads;

import com.demo.filmdb.person.Person;

import java.util.List;

public record UpdateDirectorsPayload(List<Person> directors) {
}
