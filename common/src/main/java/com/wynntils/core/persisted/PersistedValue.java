/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import com.wynntils.core.components.Managers;
import java.lang.reflect.Type;

public abstract class PersistedValue<T> implements Comparable<PersistedValue<T>> {
    private T value;

    protected PersistedValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public abstract void touched();

    public void store(T value) {
        this.value = value;
        touched();
    }

    public String getJsonName() {
        // Available after owner is registered in registerOwner()
        return Managers.Persisted.getMetadata(this).jsonName();
    }

    public Type getType() {
        // Available after owner is registered in registerOwner()
        return Managers.Persisted.getMetadata(this).valueType();
    }

    protected void setWithoutTouch(T value) {
        this.value = value;
    }

    @Override
    public int compareTo(PersistedValue<T> other) {
        return getJsonName().compareTo(other.getJsonName());
    }
}
