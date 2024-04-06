/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

public class PartyScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher PARTY_MATCHER = SegmentMatcher.fromPattern("Party:\\s\\[Lv. (\\d+)]");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return PARTY_MATCHER;
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
        return "PartyScoreboardPart{}";
    }
}
