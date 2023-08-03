/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.storage;

import com.wynntils.core.persisted.PersistedOwner;

public interface Storageable extends PersistedOwner {
    String getStorageJsonName();

    default void onStorageLoad() {}
}
