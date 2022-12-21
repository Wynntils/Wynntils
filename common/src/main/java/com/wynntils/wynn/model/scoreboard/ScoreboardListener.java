/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard;

public interface ScoreboardListener {
    void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType);

    void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType);

    void resetHandler();
}
