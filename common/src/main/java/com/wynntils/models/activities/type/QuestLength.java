/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import java.util.Locale;
import java.util.Optional;

public enum QuestLength {
    SHORT,
    MEDIUM,
    LONG;

    public static QuestLength fromString(String str) {
        try {
            return QuestLength.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Use SHORT as fallback... it's as good as any
            return SHORT;
        }
    }

    public static QuestLength fromActivityLength(Optional<ActivityLength> activityLength) {
        if (activityLength.isEmpty()) {
            // Use SHORT as fallback... it's as good as any
            return SHORT;
        }

        return switch (activityLength.get()) {
            case SHORT -> SHORT;
            case MEDIUM -> MEDIUM;
            case LONG -> LONG;
        };
    }
}
