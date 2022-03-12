/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.properties;

/**
 * How stable a feature is to wynncraft changes, how able it is to be maintained, and other similar
 * judgements. Subjective.
 */
public enum Stability {
    EXPERIMENTAL("Experimental"),
    UNSTABLE("Unstable"),
    STABLE("Stable"),
    INVARIABLE("Invariable");

    private final String displayName;

    Stability(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
