/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

public final class ErrorOr<T> {
    private final T value;
    private final String error;

    private ErrorOr(T value, String error) {
        this.value = value;
        this.error = error;
    }

    public static <T> ErrorOr<T> of(T value) {
        return new ErrorOr<>(value, null);
    }

    public static <T> ErrorOr<T> error(String error) {
        return new ErrorOr<>(null, error);
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

    @Override
    public String toString() {
        return "ErrorOr{" + "value=" + value + ", error='" + error + '\'' + '}';
    }
}
