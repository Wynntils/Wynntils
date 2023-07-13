/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

public enum ContentDistance {
    NEAR("Near"),
    MEDIUM("Medium"),
    FAR("Far");

    private final String displayName;

    ContentDistance(String displayName) {
        this.displayName = displayName;
    }

    public static ContentDistance from(String displayName) {
        for (ContentDistance distance : values()) {
            if (distance.getDisplayName().equals(displayName)) return distance;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
