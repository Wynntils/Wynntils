/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.utils.type.CappedValue;

public class MountTraitsSegment extends ActionBarSegment {
    private final CappedValue cappedEnergy;
    private final CappedValue cappedToughness;

    public MountTraitsSegment(
            String segmentText, int startIndex, int endIndex, CappedValue cappedEnergy, CappedValue cappedToughness) {
        super(segmentText, startIndex, endIndex);
        this.cappedEnergy = cappedEnergy;
        this.cappedToughness = cappedToughness;
    }

    public CappedValue getCappedEnergy() {
        return cappedEnergy;
    }

    public CappedValue getCappedToughness() {
        return cappedToughness;
    }

    @Override
    public String toString() {
        return "MountTraitsSegment{" + "endIndex="
                + endIndex + ", startIndex="
                + startIndex + ", segmentText='"
                + segmentText + '\'' + ", cappedEnergy="
                + cappedEnergy + ", cappedToughness="
                + cappedToughness + '}';
    }
}
