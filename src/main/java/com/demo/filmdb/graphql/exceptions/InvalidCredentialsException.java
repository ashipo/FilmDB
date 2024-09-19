package com.demo.filmdb.graphql.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Invalid credentials";
    }
}
