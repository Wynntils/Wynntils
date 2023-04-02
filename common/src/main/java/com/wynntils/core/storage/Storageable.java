/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.storage;

public interface Storageable {
    String getStorageJsonName();

    default void onStorageLoad() {}
}
