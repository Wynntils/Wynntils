/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

// A note on percent vs raw numbers and how they combine.
// From HeyZeer0:
// base = base + (base * percentage1) + (base * percentage2) + rawValue
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
