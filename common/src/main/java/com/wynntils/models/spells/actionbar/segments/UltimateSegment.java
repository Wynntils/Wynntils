/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.spells.type.UltimateInfo;

public class UltimateSegment extends ActionBarSegment {
    private final UltimateInfo ultimateInfo;

    public UltimateSegment(String segmentText, int startIndex, int endIndex, UltimateInfo ultimateInfo) {
        super(segmentText, startIndex, endIndex);
        this.ultimateInfo = ultimateInfo;
    }

    @Override
    public String toString() {
        return "UltimateSegment{" + "segmentText='" + segmentText + '\'' + ", ultimateInfo=" + ultimateInfo + '}';
    }
}
