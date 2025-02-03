package com.demo.filmdb.graphql.config;

import com.demo.filmdb.graphql.exceptions.InvalidCredentialsException;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.demo.filmdb.graphql.exceptions.GraphQLErrorType.CONFLICT;
import static graphql.ErrorType.ValidationError;

@Component
@SuppressWarnings("unused")
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

        return GraphqlErrorBuilder.newError(env)
                .errorType(errorType)
                .message(ex.getMessage())
                .build();
    }

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        GraphQLError singleError = resolveToSingleError(ex, env);
        if (singleError != null) {
            return Collections.singletonList(singleError);
        }

        if (ex instanceof ConstraintViolationException violationException) {
            var errors = new ArrayList<GraphQLError>();
            for (var violation : violationException.getConstraintViolations()) {
                var error = GraphqlErrorBuilder.newError(env)
                        .errorType(ValidationError)
                        .message(violation.getMessage())
                        .build();
                errors.add(error);
            }
            return errors;
        } else {
            return null;
        }
    }
}
