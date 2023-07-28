/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.google.common.collect.ImmutableMap;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.handlers.scoreboard.type.ScoreboardLine;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.mc.event.ScoreboardSetDisplayObjectiveEvent;
import com.wynntils.mc.event.ScoreboardSetObjectiveEvent;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

public final class ScoreboardHandler extends Handler {
    private static final Pattern NEXT_LINE_PATTERN = Pattern.compile("À+");
    private static final String SCOREBOARD_KEY = "wynntilsSB";
    private static final MutableComponent SCOREBOARD_TITLE_COMPONENT = Component.literal("play.wynncraft.com")
            .withStyle(ChatFormatting.BOLD)
            .withStyle(ChatFormatting.GOLD);
    private static final int MAX_SCOREBOARD_LINE = 16;
    private static final ScoreboardPart FALLBACK_SCOREBOARD_PART = new FallbackScoreboardPart();

    private String currentScoreboardName = "";
    private final Map<ScoreboardPart, ScoreboardSegment> scoreboardSegments = new LinkedHashMap<>();

    private final List<ScoreboardPart> scoreboardParts = new ArrayList<>();

    public void addPart(ScoreboardPart scoreboardPart) {
        scoreboardParts.add(scoreboardPart);
    }

    private boolean isValidScoreboardName(String scoreboardName) {
        // If the name is longer than 14 characters, we need to trim it (16 chars max, 2 reversed for sb/bf)
        String name = McUtils.player().getScoreboardName();
        name = name.length() > 14 ? name.substring(0, 14) : name;

        return (scoreboardName.startsWith("sb") || scoreboardName.startsWith("bf")) && scoreboardName.endsWith(name);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetScore(ScoreboardSetScoreEvent event) {
        if (!currentScoreboardName.equals(event.getObjectiveName())) return;

        handleUpdate();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetObjective(ScoreboardSetObjectiveEvent event) {
        if (!currentScoreboardName.equals(event.getObjectiveName())) return;

        handleUpdate();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetObjectiveDisplay(ScoreboardSetDisplayObjectiveEvent event) {
        if (!isValidScoreboardName(event.getObjectiveName())) return;

        currentScoreboardName = event.getObjectiveName();
        handleUpdate();

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) return;

        scoreboardSegments.keySet().forEach(ScoreboardPart::reset);

        scoreboardSegments.clear();
        currentScoreboardName = "";
    }

    private void handleUpdate() {
        // 1. Get a reconstructed scoreboard from the current scoreboard state
        List<ScoreboardLine> reconstructedScoreboard = getCurrentScoreboardState(currentScoreboardName);

        // 2. Verify that the scoreboard is in a semi-valid state
        // (in a state where we can make sense of it, even if the actual data is still being updated)
        if (!isScoreboardValid(reconstructedScoreboard)) return;

        // 3. Calculate the scoreboard segments, do segment updates
        calculateScoreboardSegments(reconstructedScoreboard);

        // 4. Create our own scoreboard to hide specific segments
        createScoreboardFromSegments(reconstructedScoreboard);
    }

    private List<ScoreboardLine> getCurrentScoreboardState(String currentScoreboardName) {
        Scoreboard scoreboard = McUtils.mc().level.getScoreboard();
        Objective objective = scoreboard.getObjective(currentScoreboardName);
        List<Score> lines = new ArrayList<>(scoreboard.getPlayerScores(objective));

        // Lines are by default in reverse order
        Collections.reverse(lines);

        return lines.stream()
                .map(s -> new ScoreboardLine(StyledText.fromString(s.getOwner()), s.getScore()))
                .toList();
    }

    private boolean isScoreboardValid(List<ScoreboardLine> reconstructedScoreboard) {
        // The scoreboard is valid if:
        // 1. There are no duplicate lines
        // 2. There are no gaps in the scores, and they are decreasing (there are no duplicate scores)

        // We can also check for validness by checking scoreboard parts:
        // 3. A valid scoreboard always starts with a newline (À)
        // 4. A scoreboard is valid if it consists of valid segments:
        //    - A valid segment is a part that starts with a header, then one or more lines, then a footer which is a
        // newline (À+).
        //    - The footer is not present if the segment is the last one displayed.

        // 0. An empty scoreboard is valid
        if (reconstructedScoreboard.isEmpty()) {
            return true;
        }

        // 1. Check for duplicate lines
        List<StyledText> lines = new ArrayList<>();
        for (ScoreboardLine line : reconstructedScoreboard) {
            if (lines.contains(line.line())) {
                return false;
            }

            lines.add(line.line());
        }

        // 2. Check for gaps in the scores
        int lastScore = reconstructedScoreboard.stream()
                .map(ScoreboardLine::score)
                .findFirst()
                .orElse(0);
        for (ScoreboardLine line : reconstructedScoreboard.stream().skip(1).toList()) {
            if (line.score() + 1 != lastScore) {
                return false;
            }

            lastScore = line.score();
        }

        // 3. Check for a new line at the start
        if (!reconstructedScoreboard.stream()
                .findFirst()
                .map(ScoreboardLine::line)
                .orElse(StyledText.EMPTY)
                .equals(StyledText.fromString("À"))) {
            return false;
        }

        // 4. Check for segment correctness
        int currentIndex = 1;
        List<ScoreboardLine> scoreboardLines = reconstructedScoreboard.stream().toList();

        Set<ScoreboardPart> usedParts = new HashSet<>();

        while (currentIndex < scoreboardLines.size()) {
            ScoreboardPart part = getScoreboardPartForHeader(scoreboardLines.get(currentIndex));

            // We could not find a suitable part for the header
            if (part == null || usedParts.contains(part)) {
                return false;
            }

            // The header cannot be the last line
            if (currentIndex + 1 == scoreboardLines.size()) {
                return false;
            }

            // The next line cannot be the end of this segment
            // (As it would mean the header has no content)
            if (scoreboardLines
                    .get(currentIndex + 1)
                    .line()
                    .getMatcher(NEXT_LINE_PATTERN)
                    .matches()) {
                return false;
            }

            usedParts.add(part);

            // Find the next segment end
            for (currentIndex = currentIndex + 1; currentIndex < scoreboardLines.size(); currentIndex++) {
                ScoreboardLine line = scoreboardLines.get(currentIndex);

                if (line.line().getMatcher(NEXT_LINE_PATTERN).matches()) {
                    currentIndex++;
                    break;
                }
            }
        }

        // All checks passed, so the scoreboard is valid
        // (In theory, this can happen while the scoreboard is still being updated, but it's very unlikely, and we
        // cannot do anything about it)
        return true;
    }

    private void calculateScoreboardSegments(List<ScoreboardLine> reconstructedScoreboard) {
        int currentIndex = 1;
        List<ScoreboardLine> scoreboardLines = reconstructedScoreboard.stream().toList();

        Map<ScoreboardPart, ScoreboardSegment> oldSegments = ImmutableMap.copyOf(scoreboardSegments);
        scoreboardSegments.clear();

        while (currentIndex < scoreboardLines.size()) {
            ScoreboardLine headerLine = scoreboardLines.get(currentIndex);
            ScoreboardPart part = getScoreboardPartForHeader(headerLine);

            // We could not find a suitable part for the header
            if (part == null) {
                WynntilsMod.error(
                        "Scoreboard passed validness check, but we could not find a scoreboard part for the line: "
                                + scoreboardLines.get(currentIndex).line());
                return;
            }

            List<StyledText> contentLines = new ArrayList<>();
            for (currentIndex = currentIndex + 1; currentIndex < scoreboardLines.size(); currentIndex++) {
                ScoreboardLine line = scoreboardLines.get(currentIndex);

                if (line.line().getMatcher(NEXT_LINE_PATTERN).matches()) {
                    currentIndex++;
                    break;
                }

                contentLines.add(line.line());
            }

            ScoreboardSegment segment = new ScoreboardSegment(part, headerLine.line(), contentLines);
            boolean eventCanceled = WynntilsMod.postEvent(new ScoreboardSegmentAdditionEvent(segment));

            segment.setVisibility(!eventCanceled);
            scoreboardSegments.put(part, segment);
        }

        // Handle segment removals
        for (Map.Entry<ScoreboardPart, ScoreboardSegment> entry : oldSegments.entrySet()) {
            if (scoreboardSegments.get(entry.getKey()) == null) {
                entry.getKey().onSegmentRemove(entry.getValue());
            }
        }

        // Handle segment changes
        for (Map.Entry<ScoreboardPart, ScoreboardSegment> entry : scoreboardSegments.entrySet()) {
            ScoreboardSegment oldSegment = oldSegments.get(entry.getKey());

            if (oldSegment == null || !oldSegment.equals(entry.getValue())) {
                entry.getKey().onSegmentChange(entry.getValue());
            }
        }
    }

    private void createScoreboardFromSegments(List<ScoreboardLine> reconstructedScoreboard) {
        Scoreboard scoreboard = McUtils.player().getScoreboard();

        Objective oldObjective = scoreboard.getObjective(SCOREBOARD_KEY);
        if (oldObjective != null) {
            scoreboard.removeObjective(oldObjective);
        }

        Objective wynntilsObjective = scoreboard.addObjective(
                SCOREBOARD_KEY,
                ObjectiveCriteria.DUMMY,
                SCOREBOARD_TITLE_COMPONENT,
                ObjectiveCriteria.RenderType.INTEGER);

        scoreboard.setDisplayObjective(1, wynntilsObjective);

        if (scoreboardSegments.values().stream().noneMatch(ScoreboardSegment::isVisible)) return;

        int currentScoreboardLine = MAX_SCOREBOARD_LINE;

        // Insert the first line at the top
        scoreboard.getOrCreatePlayerScore("À", wynntilsObjective).setScore(currentScoreboardLine);
        currentScoreboardLine--;

        int separatorCount = 2;

        // Insert the visible segments
        List<ScoreboardSegment> segments = scoreboardSegments.values().stream().toList();
        for (int i = 0; i < segments.size(); i++) {
            ScoreboardSegment scoreboardSegment = segments.get(i);
            if (!scoreboardSegment.isVisible()) continue;

            scoreboard
                    .getOrCreatePlayerScore(scoreboardSegment.getHeader().getString(), wynntilsObjective)
                    .setScore(currentScoreboardLine);
            currentScoreboardLine--;

            for (StyledText line : scoreboardSegment.getContent()) {
                scoreboard
                        .getOrCreatePlayerScore(line.getString(), wynntilsObjective)
                        .setScore(currentScoreboardLine);
                currentScoreboardLine--;
            }

            if (i != segments.size() - 1) {
                scoreboard
                        .getOrCreatePlayerScore(StringUtils.repeat('À', separatorCount), wynntilsObjective)
                        .setScore(currentScoreboardLine);
                currentScoreboardLine--;
                separatorCount++;
            }
        }
    }

    private ScoreboardPart getScoreboardPartForHeader(ScoreboardLine scoreboardLine) {
        String unformattedLine = scoreboardLine.line().getString(PartStyle.StyleType.NONE);

        for (ScoreboardPart part : scoreboardParts) {
            if (part.getSegmentMatcher()
                    .headerPattern()
                    .matcher(unformattedLine)
                    .matches()) {
                return part;
            }
        }

        return FALLBACK_SCOREBOARD_PART;
    }

    private static final class FallbackScoreboardPart extends ScoreboardPart {
        private static final SegmentMatcher FALLBACK_MATCHER = SegmentMatcher.fromPattern(".*");

        @Override
        public SegmentMatcher getSegmentMatcher() {
            return FALLBACK_MATCHER;
        }

        @Override
        public void onSegmentChange(ScoreboardSegment newValue) {}

        @Override
        public void onSegmentRemove(ScoreboardSegment segment) {}

        @Override
        public void reset() {}

        @Override
        public String toString() {
            return "FallbackScoreboardPart{}";
        }
    }
}
