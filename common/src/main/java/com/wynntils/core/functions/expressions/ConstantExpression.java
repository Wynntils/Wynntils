/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions;

import com.google.common.collect.ImmutableMap;
import com.wynntils.utils.type.ErrorOr;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ConstantExpression extends Expression {
    private static final Map<Class<?>, Function<String, Optional<Object>>> CONSTANT_EXPRESSION_PARSERS =
            ImmutableMap.of(
                    String.class, ConstantExpression::markedStringParser,
                    Integer.class, ConstantExpression::intParser,
                    Double.class, ConstantExpression::doubleParser,
                    Boolean.class, ConstantExpression::booleanParser);

    private final Object value;

    protected ConstantExpression(String rawExpression, Object value) {
        super(rawExpression);
        this.value = value;
    }

    @Override
    public ErrorOr<Object> calculate() {
        return ErrorOr.of(value);
    }

    @Override
    public ErrorOr<String> calculateFormattedString() {
        return ErrorOr.of(value.toString());
    }

    public static ErrorOr<Optional<Expression>> tryParse(String rawExpression) {
        return ErrorOr.of(CONSTANT_EXPRESSION_PARSERS.values().stream()
                .map(value -> value.apply(rawExpression))
                .flatMap(Optional::stream)
                .findAny()
                .map(parsedValue -> new ConstantExpression(rawExpression, parsedValue)));
    }

    // region Parsers

    private static Optional<Object> markedStringParser(String rawString) {
        if (rawString.length() > 1 && rawString.startsWith("\"") && rawString.endsWith("\"")) {
            return Optional.of(rawString.substring(1, rawString.length() - 1));
        }

        return Optional.empty();
    }

    private static Optional<Object> intParser(String rawString) {
        try {
            return Optional.of(Integer.parseInt(rawString));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Object> doubleParser(String rawString) {
        try {
            return Optional.of(Double.parseDouble(rawString));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Object> booleanParser(String rawString) {
        if (rawString.equalsIgnoreCase("true") || rawString.equalsIgnoreCase("false")) {
            return Optional.of(Boolean.parseBoolean(rawString));
        }

        return Optional.empty();
    }

    // endregion
}
