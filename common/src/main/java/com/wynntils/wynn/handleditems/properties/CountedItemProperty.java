/*
 * Copyright © Wynntils 2022, 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.properties;

public interface CountedItemProperty {
    int getCount();

    default boolean hasCount() {
        return true;
    }
}
