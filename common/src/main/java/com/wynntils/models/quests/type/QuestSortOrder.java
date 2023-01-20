/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests.type;

import java.util.Locale;

public enum QuestSortOrder {
    LEVEL,
    DISTANCE,
    ALPHABETIC;

    public static QuestSortOrder fromString(String str) {
        if (str == null || str.isEmpty()) return LEVEL;

        try {
            return QuestSortOrder.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return LEVEL;
        }
    }
}
