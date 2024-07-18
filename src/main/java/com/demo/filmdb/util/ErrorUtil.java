package com.demo.filmdb.util;

public final class ErrorUtil {
    public static String personNotFoundMessage(Long id) {
        return "Could not find person with id " + id;
    }

    public static String filmNotFoundMessage(Long id) {
        return "Could not find film with id " + id;
    }

    public static String roleNotFoundMessage(Long filmId, Long personId) {
        return "Could not find role with filmId " + filmId + " and personId " + personId;
    }
}
