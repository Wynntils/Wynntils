/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
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
        Models.GuildAttackTimer.processChanges(newValue);
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        Models.GuildAttackTimer.resetTimers();
    }

    @Override
    public void reset() {
        Models.GuildAttackTimer.resetTimers();
    }

    @Override
    public String toString() {
        return "GuildAttackScoreboardPart{}";
    }
}
