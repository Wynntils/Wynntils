/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.Set;

public class PartyScoreboardPart implements ScoreboardPart {
    private static final SegmentMatcher PARTY_MATCHER = SegmentMatcher.fromPattern("Party:\\s\\[Lv. (\\d+)]");

    @Override
    public Set<SegmentMatcher> getSegmentMatchers() {
        return Set.of(PARTY_MATCHER);
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue, SegmentMatcher segmentMatcher) {
        // Not yet implemented
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment, SegmentMatcher segmentMatcher) {
        // Not yet implemented
    }

    @Override
    public void reset() {
        // Not yet implemented
    }
}
