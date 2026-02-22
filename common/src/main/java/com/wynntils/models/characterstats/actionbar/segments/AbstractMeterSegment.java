/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public abstract class AbstractMeterSegment extends ActionBarSegment {
    protected AbstractMeterSegment(String segmentText, int startIndex, int endIndex) {
        super(segmentText, startIndex, endIndex);
    }
}
