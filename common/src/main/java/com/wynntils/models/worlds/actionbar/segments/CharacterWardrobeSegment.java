/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class CharacterWardrobeSegment extends ActionBarSegment {
    public CharacterWardrobeSegment(String segmentText, int startIndex, int endIndex) {
        super(segmentText, startIndex, endIndex);
    }

    @Override
    public String toString() {
        return "CharacterWardrobeSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
