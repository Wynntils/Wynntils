/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.crowdsource.type;

/**
 * This enum represents the game version that the data is being sent from.
 * Not every version may have a version, and versions may be skipped.
 * This enum is only updated whenever relevant changes are made to the game.
 * We may also change this enum if the mod received relevant data collection changes.
 */
public enum CrowdSourcedDataGameVersion {
    VERSION_203_HOTFIX_4("2.0.3 Hotfix 4"),
    VERSION_204_RELEASE("2.0.4 Release");

    private final String readableVersion;

    CrowdSourcedDataGameVersion(String readableVersion) {
        this.readableVersion = readableVersion;
    }

    public String getReadableVersion() {
        return readableVersion;
    }
}
