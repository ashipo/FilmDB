package com.demo.filmdb.graphql.config;

import com.demo.filmdb.graphql.exceptions.InvalidCredentialsException;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

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
}
