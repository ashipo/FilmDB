package com.demo.filmdb.graphql;

import com.demo.filmdb.graphql.config.NonBlankString;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("NonBlankString GraphQL Scalar")
public class NonBlankStringTests {

    @SuppressWarnings("unchecked")
    private final Coercing<String, String> coercing = (Coercing<String, String>) NonBlankString.INSTANCE.getCoercing();
    private final GraphQLContext context = GraphQLContext.newContext().build();
    private final Locale locale = Locale.ROOT;
    private final CoercedVariables coercedVariables = CoercedVariables.emptyVariables();

    @Nested
    @DisplayName("parseValue")
    class ParseValue {

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("com.demo.filmdb.graphql.NonBlankStringTests#blankStringProvider")
        @DisplayName("Blank String, throws CoercingParseValueException")
        void BlankString_Throws(String value) {
            assertThatExceptionOfType(CoercingParseValueException.class).isThrownBy(() ->
                    coercing.parseValue(value, context, locale));
        }

        @Test
        @DisplayName("Non-blank string, parses")
        void NonBlankString_Parses() {
            String expected = "Title";

            String actual = coercing.parseValue(expected, context, locale);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("parseLiteral")
    class ParseLiteral {

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("com.demo.filmdb.graphql.NonBlankStringTests#blankStringProvider")
        @DisplayName("Blank StringValue, throws CoercingParseLiteralException")
        void BlankStringValue_Throws(String value) {
            StringValue stringValue = new StringValue(value);
            assertThatExceptionOfType(CoercingParseLiteralException.class).isThrownBy(() ->
                    coercing.parseLiteral(stringValue, coercedVariables, context, locale));
        }

        @Test
        @DisplayName("Non-blank StringValue, parses")
        void NonBlankStringValue_Parses() {
            String expected = "Title";

            String actual = coercing.parseLiteral(new StringValue(expected), coercedVariables, context, locale);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("serialize")
    class Serialize {

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("com.demo.filmdb.graphql.NonBlankStringTests#blankStringProvider")
        @DisplayName("Blank String, throws CoercingSerializeException")
        void BlankString_Throws(String value) {
            assertThatExceptionOfType(CoercingSerializeException.class).isThrownBy(() ->
                    coercing.serialize(value, context, locale));
        }

        @Test
        @DisplayName("Non-blank String, serializes")
        void NonBlankString_Serializes() {
            String expected = "Title";

            String actual = coercing.serialize(expected, context, locale);

            assertThat(actual).isEqualTo(expected);
        }
    }

    private static Stream<Arguments> blankStringProvider() {
        return Stream.of(
                arguments(named("\"\"", "")),
                arguments(named("\"   \"", "   ")),
                arguments(named("\"\\t\"", "\t")),
                arguments(named("\"\\n\"", "\n"))
        );
    }
}
