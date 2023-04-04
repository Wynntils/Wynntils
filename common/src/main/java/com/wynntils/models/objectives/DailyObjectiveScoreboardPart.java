/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.List;

public class DailyObjectiveScoreboardPart extends AbstractObjectivesScoreboardPart {
    private static final SegmentMatcher OBJECTIVES_MATCHER =
            SegmentMatcher.fromPattern("([★⭑] )?(Daily )?Objectives?:");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return OBJECTIVES_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<WynnObjective> objectives = parseObjectives(newValue).stream()
                .filter(wynnObjective -> wynnObjective.getScore().current()
                        < wynnObjective.getScore().max())
                .toList();

        if (objectives.isEmpty()) {
            WynntilsMod.warn("Daily objective segment changed, but no objectives were parsed.");
            WynntilsMod.warn(newValue.toString());
            return;
        }

        for (WynnObjective objective : objectives) {
            if (!objective.isGuildObjective()) {
                Models.Objectives.updatePersonalObjective(objective);
            }
        }

        // filter out deleted objectives
        Models.Objectives.purgePersonalObjectives(objectives);
    }

    @Override
    public String toString() {
        return "DailyObjectiveScoreboardPart{}";
    }
}
