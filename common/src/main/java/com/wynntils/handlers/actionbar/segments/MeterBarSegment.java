/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.segments;

public class MeterBarSegment extends AbstractMeterSegment {
    public MeterBarSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "MeterBarSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
