/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

public class MeterEdgeAnimationSegment extends AbstractMeterSegment {
    public MeterEdgeAnimationSegment(String segmentText, int startIndex, int endIndex) {
        super(segmentText, startIndex, endIndex);
    }

    @Override
    public String toString() {
        return "MeterAnimationSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
