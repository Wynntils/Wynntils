/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

public record WynncraftVersion(
        String versionGroup, String majorVersion, String minorVersion, String revision, boolean isBeta) {
    public static final WynncraftVersion DEV = new WynncraftVersion("0", "0", "0", "0", false);

    @Override
    public String toString() {
        return "v" + versionGroup + "." + majorVersion + "." + minorVersion + "_" + revision + (isBeta ? "_BETA" : "");
    }
}
