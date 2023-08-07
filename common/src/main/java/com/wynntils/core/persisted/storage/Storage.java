/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.storage;

import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.PersistedValue;

public class Storage<T> extends PersistedValue<T> {
    public Storage(T value) {
        super(value);
    }

    public void store(T value) {
        this.value = value;
        touched();
    }

    @Override
    public void touched() {
        Managers.Storage.persist();
    }

    // This must only be called by StorageManager when restoring value from disk
    @SuppressWarnings("unchecked")
    void set(Object value) {
        this.value = (T) value;
    }
}
