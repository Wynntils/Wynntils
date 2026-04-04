/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class LevelSegment extends ActionBarSegment {
    private final int level;

    public LevelSegment(String segmentText, int startIndex, int endIndex, int level) {
        super(segmentText, startIndex, endIndex);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "LevelSegment{" + "level=" + level + ", segmentText='" + segmentText + '\'' + '}';
    }
}
