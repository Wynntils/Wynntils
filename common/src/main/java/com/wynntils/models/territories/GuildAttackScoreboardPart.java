/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

public class GuildAttackScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher GUILD_ATTACK_MATCHER = SegmentMatcher.fromPattern("Upcoming Attacks:");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return GUILD_ATTACK_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        Models.GuildAttackTimer.processScoreboardChanges(newValue);
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Do nothing, war timer model will clean up after itself
    }

    @Override
    public void reset() {
        // Do nothing, war timer model will clean up after itself
    }

    @Override
    public String toString() {
        return "GuildAttackScoreboardPart{}";
    }
}
