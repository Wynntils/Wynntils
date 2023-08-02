/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

public class PersistedValue<T> {
    protected T value;

    protected PersistedValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
