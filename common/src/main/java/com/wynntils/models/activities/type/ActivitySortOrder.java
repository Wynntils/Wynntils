/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import java.util.Locale;

public enum ActivitySortOrder {
    LEVEL,
    DISTANCE,
    ALPHABETIC;

    public static ActivitySortOrder fromString(String str) {
        if (str == null || str.isEmpty()) return LEVEL;

        try {
            return ActivitySortOrder.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return LEVEL;
        }
    }
}
