/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import java.util.Locale;

public enum ContentSortOrder {
    LEVEL,
    DISTANCE,
    ALPHABETIC;

    public static ContentSortOrder fromString(String str) {
        if (str == null || str.isEmpty()) return LEVEL;

        try {
            return ContentSortOrder.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return LEVEL;
        }
    }
}
