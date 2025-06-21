/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

public record WynncraftVersion(
        String versionGroup, String majorVersion, String minorVersion, String revision, boolean isBeta) {
    @Override
    public String toString() {
        return "v" + versionGroup + "." + majorVersion + "." + minorVersion + "_" + revision + (isBeta ? " BETA" : "");
    }
}
