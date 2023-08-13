/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;

public class ActivityTrackerScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher TRACKER_MATCHER = SegmentMatcher.fromPattern("Tracked (.*):");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return TRACKER_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<StyledText> content = newValue.getContent();

        if (content.isEmpty()) {
            WynntilsMod.error("TrackerScoreboardPart: content was empty.");
        }

        Matcher matcher = newValue.getHeader().getMatcher(TRACKER_MATCHER.headerPattern(), PartStyle.StyleType.NONE);

        // This should never happens, since the handler matched this before calling us
        if (!matcher.matches()) return;

        String questName = content.get(0).getNormalized().trim().getStringWithoutFormatting();

        StringBuilder nextTask = new StringBuilder();
        List<StyledText> taskLines = content.subList(1, content.size());

        for (StyledText line : taskLines) {
            nextTask.append(line.getString()
                    .replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                    .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()));
        }

        String type = matcher.group(1);

        StyledText fixedNextTask =
                StyledText.fromString(nextTask.toString().trim()).getNormalized();

        Models.Activity.updateTracker(questName, type, fixedNextTask);
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        Models.Activity.resetTracker();
    }

    @Override
    public void reset() {
        Models.Activity.resetTracker();
    }

    @Override
    public String toString() {
        return "TrackerScoreboardPart{}";
    }
}
