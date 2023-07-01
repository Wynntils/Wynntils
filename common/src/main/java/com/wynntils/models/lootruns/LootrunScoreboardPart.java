/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootruns;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

public class LootrunScoreboardPart extends ScoreboardPart {
    @Override
    public SegmentMatcher getSegmentMatcher() {
        return SegmentMatcher.fromPattern("Lootrun:");
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        // noop
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // noop
    }

    @Override
    public void reset() {
        // noop
    }

    @Override
    public String toString() {
        return "LootrunScoreboardPart{}";
    }
}
