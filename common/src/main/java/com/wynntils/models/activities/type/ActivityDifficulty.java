/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

public enum ActivityDifficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String displayName;

    ActivityDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public static ActivityDifficulty from(String displayName) {
        for (ActivityDifficulty difficulty : values()) {
            if (difficulty.getDisplayName().equals(displayName)) return difficulty;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
