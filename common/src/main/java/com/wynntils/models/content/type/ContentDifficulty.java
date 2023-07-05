/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

public enum ContentDifficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String displayName;

    ContentDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public static ContentDifficulty from(String displayName) {
        for (ContentDifficulty difficulty : values()) {
            if (difficulty.getDisplayName().equals(displayName)) return difficulty;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
