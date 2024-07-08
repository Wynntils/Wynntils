/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.segments;

public class HealthTextSegment extends AbstractTextSegment {
    public HealthTextSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "HealthTextSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
