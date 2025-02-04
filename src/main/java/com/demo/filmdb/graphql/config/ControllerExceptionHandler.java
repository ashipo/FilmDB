package com.demo.filmdb.graphql.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;

import static graphql.ErrorType.ValidationError;

@ControllerAdvice
public class ControllerExceptionHandler {

    @GraphQlExceptionHandler
    public List<GraphQLError> handle(ConstraintViolationException ex, DataFetchingEnvironment env) {
        return ex.getConstraintViolations().stream().map(v -> GraphqlErrorBuilder.newError(env)
                .errorType(ValidationError)
                .message(v.getMessage())
                .build()
        ).toList();
    }
}
