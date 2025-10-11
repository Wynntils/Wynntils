/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class ActivityTrackerScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher TRACKER_MATCHER = SegmentMatcher.fromPattern("Tracked (.*):");
    private static final Pattern SPACER_PATTERN = Pattern.compile("[^a-zA-Z\\[\\d-].*");

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

        Matcher matcher = newValue.getHeader().getMatcher(TRACKER_MATCHER.headerPattern(), StyleType.NONE);

        // This should never happens, since the handler matched this before calling us
        if (!matcher.matches()) return;

        List<String> questNameParts = new ArrayList<>();
        for (StyledText line : content) {
            if (line.startsWith("§f")) { // Indicates quest name
                questNameParts.add(line.getNormalized().trim().getStringWithoutFormatting());
            } else {
                // We only care about the first few lines
                // If other lines with §f come up later, they probably aren't the name
                break;
            }
        }

        StringBuilder nextTask = new StringBuilder();
        List<StyledText> taskLines = content.subList(questNameParts.size(), content.size());

        for (StyledText line : taskLines) {
            String unformatted = line.getString(StyleType.NONE);
            Matcher spacerMatcher = SPACER_PATTERN.matcher(unformatted);
            if (spacerMatcher.matches()) {
                // There is a special character at the start of the line, we don't need the previous space
                if (nextTask.length() - 1 >= 0) {
                    nextTask.deleteCharAt(nextTask.length() - 1);
                }
            }

            nextTask.append(line.getString()
                    .replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                    .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()));

            if (!unformatted.endsWith(" ")) {
                nextTask.append(" ");
            }
        }

        String type = matcher.group(1);

        StyledText fixedNextTask =
                StyledText.fromString(nextTask.toString().trim()).getNormalized();

        Models.Activity.updateTracker(String.join(" ", questNameParts), type, fixedNextTask);
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
