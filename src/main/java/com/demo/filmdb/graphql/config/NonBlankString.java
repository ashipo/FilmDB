package com.demo.filmdb.graphql.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.util.Locale;

public final class NonBlankString {

    private NonBlankString() {
    }

    private static final String msg = "The value must be a non-blank string";

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("NonBlankString")
            .description("A String scalar that must contain characters other than white space")
            .coercing(new GraphqlStringCoercing() {
                @Override
                public String parseValue(Object input, GraphQLContext graphQLContext, Locale locale) throws CoercingParseValueException {
                    if (input instanceof String s) {
                        if (!s.isBlank()) {
                            return s;
                        }
                    }
                    throw new CoercingParseValueException(msg);
                }

                @Override
                public String parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) throws CoercingParseLiteralException {
                    if (input instanceof StringValue stringValue) {
                        String s = stringValue.getValue();
                        if (!s.isBlank()) {
                            return s;
                        }
                    }
                    throw new CoercingParseLiteralException(msg);
                }

                @Override
                public String serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale) throws CoercingSerializeException {
                    String str = String.valueOf(dataFetcherResult);
                    if (!str.isBlank()) {
                        return str;
                    }
                    throw new CoercingSerializeException(msg);
                }
            })
            .build();
}
