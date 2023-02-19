/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions.parser;

public final class ParseErrorOr<T> {
    private final T value;
    private final String error;

    private ParseErrorOr(T value, String error) {
        this.value = value;
        this.error = error;
    }

    public static <T> ParseErrorOr<T> of(T value) {
        return new ParseErrorOr<>(value, null);
    }

    public static <T> ParseErrorOr<T> error(String error) {
        return new ParseErrorOr<>(null, error);
    }

    public T getValue() {
        if (hasError()) {
            throw new IllegalStateException("Error present.");
        }

        return value;
    }

    public String getError() {
        if (!hasError()) {
            throw new IllegalStateException("No error present.");
        }

        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
