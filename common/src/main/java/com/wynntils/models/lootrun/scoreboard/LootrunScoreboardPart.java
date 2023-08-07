/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import java.util.List;
import java.util.regex.Pattern;

public class LootrunScoreboardPart extends ScoreboardPart {
    private static final Pattern CHOOSE_BEACON_PATTERN = Pattern.compile("^Choose a beacon!$");

    // This pattern indirectly indicates that we're done with the lootrun
    private static final Pattern WARPING_BACK_PATTERN = Pattern.compile("^Warping back to camp!$");

    private static final Pattern LOOT_PATTERN = Pattern.compile("^Loot (\\d)/(\\d) chests!$");
    private static final Pattern SLAY_PATTERN = Pattern.compile("^Slay! Wave (\\d) [-—] (\\d) (Target|Mob)s? Left!$");
    private static final Pattern DESTROY_PATTERN = Pattern.compile("^Destroy the objective!$");
    private static final Pattern DEFEND_PATTERN = Pattern.compile("^Defend for (\\d+)s!$");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return SegmentMatcher.fromPattern("Lootrun:");
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<StyledText> content = newValue.getContent();

        if (newValue.getContent().isEmpty()) {
            WynntilsMod.warn("Lootrun scoreboard segment content is empty; we might be in a bad state");
            return;
        }

        StyledText currentStateLine = content.get(0);

        if (currentStateLine.matches(CHOOSE_BEACON_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.CHOOSING_BEACON, null);
        } else if (currentStateLine.matches(WARPING_BACK_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.NOT_RUNNING, null);
        } else if (currentStateLine.matches(LOOT_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.LOOT);
        } else if (currentStateLine.matches(SLAY_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.SLAY);
        } else if (currentStateLine.matches(DESTROY_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.DESTROY);
        } else if (currentStateLine.matches(DEFEND_PATTERN, PartStyle.StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.DEFEND);
        }
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Do nothing here, we will know when the lootrun is over from patterns
    }

    @Override
    public void reset() {
        // Do nothing here, we will know when the lootrun is over from patterns
    }

    @Override
    public String toString() {
        return null;
    }
}
