/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.services.athena.type.WynncraftVersion;

public class WynncraftVersionSegment extends ActionBarSegment {
    private final WynncraftVersion wynncraftVersion;

    public WynncraftVersionSegment(
            String segmentText, int startIndex, int endIndex, WynncraftVersion wynncraftVersion) {
        super(segmentText, startIndex, endIndex);
        this.wynncraftVersion = wynncraftVersion;
    }

    public WynncraftVersion getWynncraftVersion() {
        return wynncraftVersion;
    }

    @Override
    public String toString() {
        return "WynncraftVersionSegment{" + "wynncraftVersion='"
                + wynncraftVersion + '\'' + ", segmentText='"
                + segmentText + '\'' + '}';
    }
}
