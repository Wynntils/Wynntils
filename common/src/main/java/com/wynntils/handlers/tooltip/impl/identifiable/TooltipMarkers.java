/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

public enum TooltipMarkers {
    ALIGN_CENTER("wynntils:gear-align-center"),
    ALIGN_RIGHT("wynntils:gear-align-right"),
    SECTION_DIVIDER("wynntils:gear-section-divider"),
    IDENTIFICATION_DIVIDER("wynntils:gear-identification-divider"),
    REROLL_BANNER("wynntils:gear-reroll-banner");

    private final String token;

    TooltipMarkers(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public boolean matches(String insertion) {
        return token.equals(insertion);
    }

    public static TooltipMarkers fromToken(String insertion) {
        if (insertion == null) {
            return null;
        }

        for (TooltipMarkers marker : values()) {
            if (marker.matches(insertion)) {
                return marker;
            }
        }

        return null;
    }
}
