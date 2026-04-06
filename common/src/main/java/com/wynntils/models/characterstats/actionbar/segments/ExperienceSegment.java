/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.utils.type.CappedValue;

public abstract class ExperienceSegment extends ActionBarSegment {
    protected final CappedValue progress;

    protected ExperienceSegment(String segmentText, int startIndex, int endIndex, CappedValue progress) {
        super(segmentText, startIndex, endIndex);
        this.progress = progress;
    }

    public CappedValue getProgress() {
        return progress;
    }
}
