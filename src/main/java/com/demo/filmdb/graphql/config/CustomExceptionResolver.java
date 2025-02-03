package com.demo.filmdb.graphql.config;

import com.demo.filmdb.graphql.exceptions.InvalidCredentialsException;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.demo.filmdb.graphql.exceptions.GraphQLErrorType.CONFLICT;

@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorClassification errorType;
        if (ex instanceof InvalidCredentialsException
                || ex instanceof EntityNotFoundException) {
            errorType = ErrorType.NOT_FOUND;
        } else if (ex instanceof EntityAlreadyExistsException) {
            errorType = CONFLICT;
        } else {
            return null;
        }

        return GraphQLError.newError()
                .errorType(errorType)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof ConstraintViolationException violationException) {
            var errors = new ArrayList<GraphQLError>();
            for (var violation : violationException.getConstraintViolations()) {
                var error = GraphQLError.newError()
                        .errorType(ErrorType.BAD_REQUEST)
                        .message(violation.getMessage())
                        .path(env.getExecutionStepInfo().getPath())
                        .location(env.getField().getSourceLocation())
                        .build();
                errors.add(error);
            }
            return errors;
        } else {
            return null;
        }
    }
}
