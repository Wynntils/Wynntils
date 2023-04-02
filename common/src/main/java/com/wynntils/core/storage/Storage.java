/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.storage;

import com.wynntils.core.components.Managers;

public class Storage<T> {
    private T value;

    public Storage(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void store(T value) {
        this.value = value;
        touched();
    }

    public void touched() {
        Managers.Storage.persist();
    }

    // This must only be called by StorageManager when restoring value from disk
    @SuppressWarnings("unchecked")
    void set(Object value) {
        this.value = (T) value;
    }
}
