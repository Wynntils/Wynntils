/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.utils.type.CappedValue;

public class TimeTrialCheckpointsSegment extends ActionBarSegment {
    private final CappedValue checkpoints;

    public TimeTrialCheckpointsSegment(String segmentText, int startIndex, int endIndex, CappedValue checkpoints) {
        super(segmentText, startIndex, endIndex);

        this.checkpoints = checkpoints;
    }

    public CappedValue getCheckpoints() {
        return checkpoints;
    }

    @Override
    public String toString() {
        return "TimeTrialCheckpointsSegment{" + "checkpoints="
                + checkpoints + ", segmentText='"
                + segmentText + '\'' + ", startIndex="
                + startIndex + ", endIndex="
                + endIndex + '}';
    }
}
