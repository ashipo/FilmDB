package com.demo.filmdb.util;

import com.demo.filmdb.annotations.Sortable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SortUtil {

    /**
     * Filters out {@code pageable}'s {@link org.springframework.data.domain.Sort} properties that aren't annotated with @{@link Sortable} in the
     * {@code entityClass}
     *
     * @param pageable    containing the {@link org.springframework.data.domain.Sort} to filter
     * @param entityClass containing fields to filter by
     * @return a {@link Pageable} with the filtered {@link org.springframework.data.domain.Sort}
     */
    public static Pageable filterSortableFields(Pageable pageable, Class<?> entityClass) {
        Set<String> sortableFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.getAnnotation(Sortable.class) != null)
                .map(Field::getName)
                .collect(Collectors.toSet());
        List<org.springframework.data.domain.Sort.Order> filteredOrders = pageable.getSort()
                .filter(order -> sortableFields.contains(order.getProperty()))
                .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), org.springframework.data.domain.Sort.by(filteredOrders));
    }
}
