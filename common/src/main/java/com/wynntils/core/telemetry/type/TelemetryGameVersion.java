/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry.type;

/**
 * This enum represents the game version that the telemetry is being sent from.
 * Not every version may have a telemetry version, and telemetry versions may be skipped.
 * This enum is only updated whenever relevant changes are made to the game.
 */
public enum TelemetryGameVersion {
    VERSION_203_HOTFIX_4("2.0.3 Hotfix 4");

    private final String readableVersion;

    TelemetryGameVersion(String readableVersion) {
        this.readableVersion = readableVersion;
    }

    public String getReadableVersion() {
        return readableVersion;
    }
}
