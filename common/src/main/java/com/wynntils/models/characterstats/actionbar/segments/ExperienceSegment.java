/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.utils.type.CappedValue;

public abstract class ExperienceSegment extends ActionBarSegment {
    protected final CappedValue progress;

    protected ExperienceSegment(String segmentText, CappedValue progress) {
        super(segmentText);
        this.progress = progress;
    }

    public CappedValue getProgress() {
        return progress;
    }
}
