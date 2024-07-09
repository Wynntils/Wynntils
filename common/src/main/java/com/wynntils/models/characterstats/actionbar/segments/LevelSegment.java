/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class LevelSegment extends ActionBarSegment {
    public LevelSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "LevelSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
