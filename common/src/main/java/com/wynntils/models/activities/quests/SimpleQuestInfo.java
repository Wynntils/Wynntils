/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.quests;

public record SimpleQuestInfo(String displayName, int level, boolean removed) {
    public static final SimpleQuestInfo UNKNOWN = new SimpleQuestInfo("Unknown", 0, false);
}
