/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

public enum ActivityDistance {
    VERY_NEAR("Very Near"), // This is a special case for the "Nearby" activity, getting this to show is not consistent
    NEAR("Near"),
    MEDIUM("Medium"),
    FAR("Far");

    private final String displayName;

    ActivityDistance(String displayName) {
        this.displayName = displayName;
    }

    public static ActivityDistance from(String displayName) {
        for (ActivityDistance distance : values()) {
            if (distance.getDisplayName().equals(displayName)) return distance;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
