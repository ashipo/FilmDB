package com.demo.filmdb.utils;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.demo.filmdb.utils.SortUtil.filterSort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

@SpringBootTest
public class SortUtilTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("sortDataProvider")
    @DisplayName("filterSort for entity")
    void filterSort_EntityClass_Filters(Class<?> entityClass, List<String> sortableFields, String[] sortProperties) {
        Pageable givenPageable = PageRequest.of(0, 20, Sort.by(sortProperties));
        Condition<String> sortable = new Condition<>(sortableFields::contains,
                "a field annotated with @Sortable");

        Sort actualSort = filterSort(givenPageable, entityClass).getSort();

        Set<String> actualProperties = actualSort.map(Sort.Order::getProperty).toSet();
        assertThat(actualProperties).are(sortable);
    }

    private static Stream<Arguments> sortDataProvider() {
        return Stream.of(
                Arguments.arguments(named("Film", Film.class),
                        List.of("id", "title", "releaseDate"),
                        new String[]{"id", "title", "releaseDate", "synopsis", "directors", "cast", "roles", "dob", "name"}),
                Arguments.arguments(named("Person", Person.class),
                        List.of("id", "name", "dob"),
                        new String[]{"id", "name", "dob", "filmsDirected", "roles", "releaseDate", "title"})
        );
    }
}
