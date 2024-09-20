package com.demo.filmdb.utils;

import com.demo.filmdb.annotations.Sortable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SortUtil {

    /**
     * Filters out {@code pageable}'s {@link Sort} properties that aren't annotated with @{@link Sortable} in the
     * {@code entityClass}.
     * @param pageable containing the {@link Sort} to filter.
     * @param entityClass containing fields to filter by.
     * @return a {@link Pageable} with the filtered {@link Sort}.
     */
    public static Pageable filterSort(Pageable pageable, Class<?> entityClass) {
        Set<String> sortableFields = Arrays.stream(entityClass.getDeclaredFields()).
                filter(f -> f.getAnnotation(Sortable.class) != null).
                map(Field::getName).collect(Collectors.toSet());
        List<Sort.Order> filteredOrders = pageable.getSort().
                filter(order -> sortableFields.contains(order.getProperty())).toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(filteredOrders));
    }
}
