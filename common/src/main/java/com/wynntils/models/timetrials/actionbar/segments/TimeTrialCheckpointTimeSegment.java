/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class TimeTrialCheckpointTimeSegment extends ActionBarSegment {
    private final long checkpointTime;

    public TimeTrialCheckpointTimeSegment(String segmentText, int startIndex, int endIndex, long checkpointTime) {
        super(segmentText, startIndex, endIndex);
        this.checkpointTime = checkpointTime;
    }

    public long getCheckpointTime() {
        return checkpointTime;
    }

    @Override
    public String toString() {
        return "TimeTrialCheckpointTimeSegment{" + "checkpointTime="
                + checkpointTime + ", segmentText='"
                + segmentText + '\'' + ", startIndex="
                + startIndex + ", endIndex="
                + endIndex + '}';
    }
}
