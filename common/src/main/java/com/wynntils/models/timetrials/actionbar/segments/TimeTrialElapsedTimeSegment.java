/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class TimeTrialElapsedTimeSegment extends ActionBarSegment {
    private final long elapsedTime;

    public TimeTrialElapsedTimeSegment(String segmentText, int startIndex, int endIndex, long elapsedTime) {
        super(segmentText, startIndex, endIndex);
        this.elapsedTime = elapsedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public String toString() {
        return "TimeTrialElapsedTimeSegment{" + "elapsedTime="
                + elapsedTime + ", segmentText='"
                + segmentText + '\'' + ", startIndex="
                + startIndex + ", endIndex="
                + endIndex + '}';
    }
}
