/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

public enum ActivityLength {
    SHORT("Short"),
    MEDIUM("Medium"),
    LONG("Long");

    private final String displayName;

    ActivityLength(String displayName) {
        this.displayName = displayName;
    }

    public static ActivityLength from(String displayName) {
        for (ActivityLength length : values()) {
            if (length.getDisplayName().equals(displayName)) return length;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
