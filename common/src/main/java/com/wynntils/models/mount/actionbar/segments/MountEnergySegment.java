/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.utils.type.CappedValue;

public class MountEnergySegment extends ActionBarSegment {
    private final CappedValue cappedEnergy;

    public MountEnergySegment(String segmentText, int startIndex, int endIndex, CappedValue cappedEnergy) {
        super(segmentText, startIndex, endIndex);
        this.cappedEnergy = cappedEnergy;
    }

    public CappedValue getCappedEnergy() {
        return cappedEnergy;
    }

    @Override
    public String toString() {
        return "MountEnergySegment{" + "endIndex="
                + endIndex + ", startIndex="
                + startIndex + ", segmentText='"
                + segmentText + '\'' + ", cappedEnergy="
                + cappedEnergy + '}';
    }
}
