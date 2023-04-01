/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

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
        List<WynnObjective> objectives = parseObjectives(newValue).stream()
                .filter(wynnObjective -> wynnObjective.getScore().current()
                        < wynnObjective.getScore().max())
                .toList();

        for (WynnObjective objective : objectives) {
            if (objective.isGuildObjective()) {
                Models.Objectives.updateGuildObjective(objective);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "GuildObjectiveScoreboardPart{}";
    }
}
