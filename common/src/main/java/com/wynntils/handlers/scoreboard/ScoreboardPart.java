/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

/**
 * This is the "segment handler" that is implemented by the different models that want to
 * take part in the scoreboard handler.
 */
public abstract class ScoreboardPart {
    public abstract SegmentMatcher getSegmentMatcher();

    public abstract void onSegmentChange(ScoreboardSegment newValue);

    public abstract void onSegmentRemove(ScoreboardSegment segment);

    public abstract void reset();

    @Override
    public abstract String toString();
}
