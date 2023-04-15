/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

public class WarScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher WAR_MATCHER = SegmentMatcher.fromPattern("War:");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return WAR_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        // Not yet implemented
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Not yet implemented
    }

    @Override
    public void reset() {
        // Not yet implemented
    }

    @Override
    public String toString() {
        return "WarScoreboardPart{}";
    }
}
