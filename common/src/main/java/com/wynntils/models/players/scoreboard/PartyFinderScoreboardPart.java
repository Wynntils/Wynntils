/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

public class PartyFinderScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher PARTY_FINDER_MATCHER = SegmentMatcher.fromPattern("Party Finder:");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return PARTY_FINDER_MATCHER;
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
        return "PartyFinderScoreboardPart{}";
    }
}
