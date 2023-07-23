/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests.type;

import com.wynntils.models.activities.type.ActivityLength;
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

    public static QuestLength fromContentLength(Optional<ActivityLength> contentLength) {
        if (contentLength.isEmpty()) {
            // Use SHORT as fallback... it's as good as any
            return SHORT;
        }

        return switch (contentLength.get()) {
            case SHORT -> SHORT;
            case MEDIUM -> MEDIUM;
            case LONG -> LONG;
        };
    }
}
