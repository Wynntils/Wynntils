/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.wynntils.core.WynntilsMod;

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

    public ErrorOr<T> logged() {
        if (!hasError()) {
            throw new IllegalStateException("Tried to log when no error is present.");
        }

        WynntilsMod.error(error);
        return (ErrorOr<T>) this;
    }

    @Override
    public String toString() {
        return "ErrorOr{" + "value=" + value + ", error='" + error + '\'' + '}';
    }
}
