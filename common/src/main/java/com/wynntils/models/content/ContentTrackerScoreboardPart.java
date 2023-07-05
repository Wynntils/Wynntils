/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;

public class ContentTrackerScoreboardPart extends ScoreboardPart {
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

        StringBuilder questName = new StringBuilder();
        StringBuilder nextTask = new StringBuilder();

        for (StyledText line : content) {
            if (line.startsWith("§f")) {
                questName.append(line.getString(PartStyle.StyleType.NONE)).append(" ");
            } else {
                nextTask.append(line.getString()
                                .replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                                .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()))
                        .append(" ");
            }
        }

        String unformattedHeader = newValue.getHeader().getString(PartStyle.StyleType.NONE);
        Matcher matcher = TRACKER_MATCHER.headerPattern().matcher(unformattedHeader);

        // This should never happens, since the handler matched this before calling us
        if (!matcher.matches()) return;

        String type = matcher.group(1);

        String fixedName = WynnUtils.normalizeBadString(questName.toString().trim());
        StyledText fixedNextTask =
                StyledText.fromString(nextTask.toString().trim()).getNormalized();

        Models.ContentTracker.updateTracker(type, fixedName, fixedNextTask);
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        Models.ContentTracker.resetTracker();
    }

    @Override
    public void reset() {
        Models.ContentTracker.resetTracker();
    }

    @Override
    public String toString() {
        return "TrackerScoreboardPart{}";
    }
}
