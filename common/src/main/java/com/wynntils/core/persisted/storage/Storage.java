/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.storage;

import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.PersistedValue;

public class Storage<T> extends PersistedValue<T> {
    public Storage(T value) {
        super(value);
    }

    @Override
    public void touched() {
        Managers.Storage.persist();
    }
}
