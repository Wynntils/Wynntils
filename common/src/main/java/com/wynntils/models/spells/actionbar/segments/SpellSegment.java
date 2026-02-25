/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.spells.type.SpellDirection;
import java.util.Arrays;

public class SpellSegment extends ActionBarSegment {
    private final SpellDirection[] directions;

    public SpellSegment(String segmentText, int startIndex, int endIndex, SpellDirection[] directions) {
        super(segmentText, startIndex, endIndex);
        this.directions = directions;

        assert directions.length <= 3;
    }

    public SpellDirection[] getDirections() {
        return directions;
    }

    @Override
    public String toString() {
        return "SpellSegment{" + "directions="
                + Arrays.toString(directions) + ", segmentText='"
                + segmentText + '\'' + '}';
    }
}
