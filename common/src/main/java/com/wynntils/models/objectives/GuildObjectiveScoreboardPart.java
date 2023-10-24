/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.List;

public class GuildObjectiveScoreboardPart extends AbstractObjectivesScoreboardPart {
    private static final SegmentMatcher GUILD_OBJECTIVES_MATCHER =
            SegmentMatcher.fromPattern("([★⭑] )?Guild Obj: (.+)");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return GUILD_OBJECTIVES_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<WynnObjective> objectives = parseObjectives(newValue);

        if (objectives.isEmpty()) {
            WynntilsMod.warn("Guild objective segment changed, but no objectives were parsed.");
            WynntilsMod.warn(newValue.toString());
            return;
        }

        if (Models.Objectives.getGuildObjective() == null) {
            WynntilsMod.info("Adding " + objectives.size() + " guild objectives.");
        }

        for (WynnObjective objective : objectives) {
            if (objective.isGuildObjective()) {
                Models.Objectives.updateGuildObjective(objective);
                break;
            }
        }
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Remove all objectives of this type
        removeAllOfType();
    }

    private static void removeAllOfType() {
        WynnObjective guildObjective = Models.Objectives.getGuildObjective();
        if (guildObjective != null) {
            Models.Objectives.removeObjective(guildObjective);
        }
    }

    @Override
    public String toString() {
        return "GuildObjectiveScoreboardPart{}";
    }
}
