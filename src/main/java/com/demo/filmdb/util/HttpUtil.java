package com.demo.filmdb.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public final class HttpUtil {

    /**
     * Throws 404 if {@code entity} is {@code null} or returns it otherwise.
     *
     * @param entity to check.
     * @param message error message supplier.
     * @return the entity.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if {@code entity} is {@code null}.
     */
    public static <T> T require(T entity, Supplier<String> message) {
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message.get());
        }
        return entity;
    }
}
