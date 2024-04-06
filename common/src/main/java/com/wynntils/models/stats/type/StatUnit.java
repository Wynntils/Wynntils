/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public enum StatUnit {
    RAW(""),
    PERCENT("%"),
    PER_3_S("/3s"),
    PER_5_S("/5s"),
    TIER(" tier");

    private final String displayName;

    StatUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
