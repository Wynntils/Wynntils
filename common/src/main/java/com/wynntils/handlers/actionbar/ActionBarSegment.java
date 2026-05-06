/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

public abstract class ActionBarSegment {
    protected final String segmentText;
    protected final int startIndex;
    protected final int endIndex;

    protected ActionBarSegment(String segmentText, int startIndex, int endIndex) {
        this.segmentText = segmentText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getSegmentText() {
        return segmentText;
    }

    /**
     * @return The start index (inclusive) of this segment in the unformatted action bar string.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @return The end index (exclusive) of this segment in the unformatted action bar string.
     */
    public int getEndIndex() {
        return endIndex;
    }
}
