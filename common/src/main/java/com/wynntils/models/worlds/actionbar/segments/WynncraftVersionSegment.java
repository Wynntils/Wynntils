/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.worlds.type.WynncraftVersion;

public class WynncraftVersionSegment extends ActionBarSegment {
    private final WynncraftVersion wynncraftVersion;

    public WynncraftVersionSegment(String segmentText, WynncraftVersion wynncraftVersion) {
        super(segmentText);
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
