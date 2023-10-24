/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.ArrayList;
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
        if (isSegmentAllDone(newValue)) {
            WynntilsMod.info("Daily objectives were all done.");
            removeAllOfType();
            return;
        }

        List<WynnObjective> objectives = parseObjectives(newValue);

        if (objectives.isEmpty()) {
            WynntilsMod.warn("Daily objective segment changed, but no objectives were parsed.");
            WynntilsMod.warn(newValue.toString());
            return;
        }

        if (Models.Objectives.getPersonalObjectives().isEmpty()) {
            WynntilsMod.info("Adding " + objectives.size() + " daily objectives.");
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
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Remove all objectives of this type
        removeAllOfType();
    }

    private static void removeAllOfType() {
        List<WynnObjective> personalObjectives = new ArrayList<>(Models.Objectives.getPersonalObjectives());
        personalObjectives.forEach(Models.Objectives::removeObjective);
    }

    @Override
    public String toString() {
        return "DailyObjectiveScoreboardPart{}";
    }
}
