/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

/**
 * Represents the compatibility between the current Wynncraft version and the Wynntils version
 */
public enum CompatibilityTier {
    // Majority of the mod will not function or major gameplay affecting issues
    INCOMPATIBLE("service.wynntils.compatibility.incompatible", null),
    // Not recommended for use, there are issues that can be avoided
    MAJOR_ERRORS("service.wynntils.compatibility.majorErrors", null),
    // Playable with a few minor issues
    MINOR_ERRORS(null, "service.wynntils.compatibility.minorErrors"),
    // Fully compatible
    COMPATIBLE(null, null),
    // Athena has no knowledge of this version or failed to check compatibility
    UNKNOWN(null, "service.wynntils.compatibility.unknown");

    private final String screenPromptKey;
    private final String chatPromptKey;

    CompatibilityTier(String screenPromptKey, String chatPromptKey) {
        this.screenPromptKey = screenPromptKey;
        this.chatPromptKey = chatPromptKey;
    }

    public boolean shouldScreenPrompt() {
        return screenPromptKey != null;
    }

    public String getScreenPromptKey() {
        return screenPromptKey;
    }

    public boolean shouldChatPrompt() {
        return chatPromptKey != null;
    }

    public String getChatPromptKey() {
        return chatPromptKey;
    }
}
