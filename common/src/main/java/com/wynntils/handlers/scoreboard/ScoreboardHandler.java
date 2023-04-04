/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.google.common.collect.ImmutableMap;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.handlers.scoreboard.type.ScoreboardLine;
import com.wynntils.mc.event.ScoreboardSetDisplayObjectiveEvent;
import com.wynntils.mc.event.ScoreboardSetObjectiveEvent;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ScoreboardHandler extends Handler {
    private static final Pattern NEXT_LINE_PATTERN = Pattern.compile("À+");
    private static final String SCOREBOARD_KEY = "wynntilsSB";
    private static final MutableComponent SCOREBOARD_TITLE_COMPONENT = Component.literal("play.wynncraft.com")
            .withStyle(ChatFormatting.BOLD)
            .withStyle(ChatFormatting.GOLD);
    private static final int MAX_SCOREBOARD_LINE = 16;

    private Set<ScoreboardLine> reconstructedScoreboard = new TreeSet<>();
    private Map<ScoreboardPart, ScoreboardSegment> scoreboardSegments = new LinkedHashMap<>();

    private List<ScoreboardPart> scoreboardParts = new ArrayList<>();

    public void addPart(ScoreboardPart scoreboardPart) {
        scoreboardParts.add(scoreboardPart);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetScore(ScoreboardSetScoreEvent event) {
        if (!Objects.equals(event.getObjectiveName(), "sb" + McUtils.player().getScoreboardName())) {
            return;
        }

        handleSetScore(event.getOwner(), event.getScore(), event.getMethod());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetObjective(ScoreboardSetObjectiveEvent event) {
        if (!Objects.equals(event.getObjectiveName(), "sb" + McUtils.player().getScoreboardName())) {
            return;
        }

        if (event.getMethod() != ScoreboardSetObjectiveEvent.METHOD_REMOVE) return;

        // Reset the scoreboard
        reconstructedScoreboard.clear();

        // Reset the scoreboard segments
        calculateScoreboardSegments();
        createScoreboardFromSegments();
    }

    @SubscribeEvent
    public void onSetObjectiveDisplay(ScoreboardSetDisplayObjectiveEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) return;

        scoreboardSegments.keySet().forEach(ScoreboardPart::reset);

        reconstructedScoreboard.clear();
        scoreboardSegments.clear();
    }

    private void handleSetScore(StyledText owner, int score, ServerScoreboard.Method method) {
        // 1. Handle the current change
        switch (method) {
            case CHANGE -> handleScoreChange(owner, score);
            case REMOVE -> handleScoreRemove(owner);
        }

        // 2. Verify that the scoreboard is in a valid state (not in a state where we are "waiting" for other packets)
        if (!isScoreboardValid()) return;

        // 3. Calculate the scoreboard segments, do segment updates
        calculateScoreboardSegments();

        // 4. Create our own scoreboard to hide specific segments
        createScoreboardFromSegments();
    }

    private void handleScoreChange(StyledText owner, int score) {
        // A score change can mean two things:
        // 1. A new line was added to the scoreboard
        // 2. An existing line with the same score was changed
        // 3. An existing line's score was changed

        // First, we check if the line already exists, by using the score as the identifier
        ScoreboardLine existingScore = reconstructedScoreboard.stream()
                .filter(line -> line.score() == score)
                .findFirst()
                .orElse(null);

        if (existingScore != null) {
            // The line already exists, so we just update the line
            reconstructedScoreboard.remove(existingScore);
            reconstructedScoreboard.add(new ScoreboardLine(owner, score));
            return;
        }

        // Secondly, we check if the line already exists, by using the owner as the identifier
        ScoreboardLine existingLine = reconstructedScoreboard.stream()
                .filter(line -> line.line().equals(owner))
                .findFirst()
                .orElse(null);

        if (existingLine != null) {
            // The line already exists, so we just update the score
            reconstructedScoreboard.remove(existingLine);
            reconstructedScoreboard.add(new ScoreboardLine(owner, score));
            return;
        }

        // The line doesn't exist, so we add it
        reconstructedScoreboard.add(new ScoreboardLine(owner, score));
    }

    private void handleScoreRemove(StyledText owner) {
        // A score remove can mean two things:
        // 1. An existing line was removed
        // 2. The line to be removed was changed before, so it doesn't exist anymore

        // First, we check if the line exists, and remove it
        // If it doesn't, we don't need to do anything
        reconstructedScoreboard.stream()
                .filter(line -> line.line().equals(owner))
                .findFirst()
                .ifPresent(existingLine -> reconstructedScoreboard.remove(existingLine));
    }

    private boolean isScoreboardValid() {
        // The scoreboard is valid if:
        // 1. There are no duplicate lines
        // 2. There are no gaps in the scores, and they are decreasing (there are no duplicate scores)

        // We can also check for validness by checking scoreboard parts:
        // 3. A valid scoreboard always starts with a new line (À)
        // 4. A valid scoreboard if it consists of valid segments:
        //    - A valid segment is a part that stats with a header, then one or more lines, then a footer which is (À+).
        //    - The footer is not present if the segment is the last one.

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
                .equals(StyledText.of("À"))) {
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
                    .match(NEXT_LINE_PATTERN)
                    .matches()) {
                return false;
            }

            usedParts.add(part);

            // Find the next segment end
            for (currentIndex = currentIndex + 1; currentIndex < scoreboardLines.size(); currentIndex++) {
                ScoreboardLine line = scoreboardLines.get(currentIndex);

                if (line.line().match(NEXT_LINE_PATTERN).matches()) {
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

    private void calculateScoreboardSegments() {
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

                if (line.line().match(NEXT_LINE_PATTERN).matches()) {
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

    private void createScoreboardFromSegments() {
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

        // Insert the visible segments
        for (ScoreboardSegment scoreboardSegment : scoreboardSegments.values()) {
            if (!scoreboardSegment.isVisible()) continue;

            scoreboard
                    .getOrCreatePlayerScore(
                            scoreboardSegment.getHeader().getInternalCodedStringRepresentation(), wynntilsObjective)
                    .setScore(currentScoreboardLine);
            currentScoreboardLine--;

            for (StyledText line : scoreboardSegment.getContent()) {
                scoreboard
                        .getOrCreatePlayerScore(line.getInternalCodedStringRepresentation(), wynntilsObjective)
                        .setScore(currentScoreboardLine);
                currentScoreboardLine--;
            }
        }
    }

    private ScoreboardPart getScoreboardPartForHeader(ScoreboardLine scoreboardLine) {
        String unformattedLine = ComponentUtils.stripFormatting(scoreboardLine.line());

        for (ScoreboardPart part : scoreboardParts) {
            if (part.getSegmentMatcher()
                    .headerPattern()
                    .matcher(unformattedLine)
                    .matches()) {
                return part;
            }
        }

        return null;
    }
}
