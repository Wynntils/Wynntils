/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

public class MeterTransitionSegment extends AbstractMeterSegment {
    public MeterTransitionSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "MeterTransitionSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
