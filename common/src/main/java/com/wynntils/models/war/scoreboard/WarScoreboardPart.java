/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.scoreboard;

import com.wynntils.core.components.Models;
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
        Models.War.onWarStart();
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        Models.War.onWarEnd();
    }

    @Override
    public void reset() {
        Models.War.onWarEnd();
    }

    @Override
    public String toString() {
        return "WarScoreboardPart{}";
    }
}
