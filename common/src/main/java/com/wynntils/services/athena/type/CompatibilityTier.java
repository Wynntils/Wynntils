/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

/**
 * Represents the compatibility between the current Wynncraft version and the Wynntils version
 */
public enum CompatibilityTier {
    // Majority of the mod will not function or major gameplay affecting issues
    INCOMPATIBLE("service.wynntils.compatibility.incompatible", true, false),
    // Not recommended for use, there are issues that can be avoided
    MAJOR_ERRORS("service.wynntils.compatibility.majorErrors", true, false),
    // Playable with a few minor issues
    MINOR_ERRORS("service.wynntils.compatibility.minorErrors", false, true),
    // Fully compatible
    COMPATIBLE(null, false, false),
    // Athena has no knowledge of this version or failed to check compatibility
    UNKNOWN("service.wynntils.compatibility.unknown", false, true);

    private final String warningKey;
    private final boolean shouldScreenPrompt;
    private final boolean shouldToastPrompt;

    CompatibilityTier(String warningKey, boolean shouldScreenPrompt, boolean shouldToastPrompt) {
        this.warningKey = warningKey;
        this.shouldScreenPrompt = shouldScreenPrompt;
        this.shouldToastPrompt = shouldToastPrompt;
    }

    public boolean shouldScreenPrompt() {
        return shouldScreenPrompt;
    }

    public boolean shouldChatPrompt() {
        return shouldToastPrompt;
    }

    public String getWarningKey() {
        return warningKey;
    }
}
