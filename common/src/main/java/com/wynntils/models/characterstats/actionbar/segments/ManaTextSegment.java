/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

public class ManaTextSegment extends AbstractTextSegment {
    public ManaTextSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "ManaTextSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
