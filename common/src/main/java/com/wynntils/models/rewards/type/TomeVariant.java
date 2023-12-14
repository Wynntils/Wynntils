/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

public enum TomeVariant {
    MASTERY; // Only variant as of writing

    public static TomeVariant fromString(String str) {
        for (TomeVariant value : TomeVariant.values()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }

        return null;
    }
}
