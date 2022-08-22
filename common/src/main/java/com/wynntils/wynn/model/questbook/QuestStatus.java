/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.questbook;

import java.util.Locale;

public enum QuestStatus {
    COMPLETED,
    STARTED,
    CAN_START,
    CANNOT_START;

    public static QuestStatus fromString(String str) {
        try {
            return QuestStatus.valueOf(str.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Use CANNOT_START as fallback... it's as good as any
            return CANNOT_START;
        }
    }
}
