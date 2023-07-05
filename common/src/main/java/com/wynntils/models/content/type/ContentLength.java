/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

public enum ContentLength {
    SHORT("Short"),
    MEDIUM("Medium"),
    LONG("Long");

    private final String displayName;

    ContentLength(String displayName) {
        this.displayName = displayName;
    }

    public static ContentLength from(String displayName) {
        for (ContentLength length : values()) {
            if (length.getDisplayName().equals(displayName)) return length;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
