/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.Set;

/**
 * This is the "segment handler" that is implemented by the different models that want to
 * take part in the scoreboard handler.
 */
public interface ScoreboardPart {
    Set<SegmentMatcher> getSegmentMatchers();

    void onSegmentChange(ScoreboardSegment newValue, SegmentMatcher segmentMatcher);

    void onSegmentRemove(ScoreboardSegment segment, SegmentMatcher segmentMatcher);

    void reset();
}
