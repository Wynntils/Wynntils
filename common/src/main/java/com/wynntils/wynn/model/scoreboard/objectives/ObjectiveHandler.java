/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wynn.model.scoreboard.ScoreboardHandler;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import com.wynntils.wynn.model.scoreboard.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectiveHandler implements ScoreboardHandler {
    // §b is guild objective, §a is normal objective and §c is daily objective
    private static final Pattern OBJECTIVE_PATTERN_ONE_LINE =
            Pattern.compile("^§([abc])[- ]\\s§7(.*): *§f(\\d+)§7/(\\d+)$");
    private static final Pattern OBJECTIVE_PATTERN_MULTILINE_START = Pattern.compile("^§([abc])[- ]\\s§7(.*)$");
    private static final Pattern OBJECTIVE_PATTERN_MULTILINE_END = Pattern.compile(".*§f(\\d+)§7/(\\d+)$");
    private static final Pattern SEGMENT_HEADER = Pattern.compile("^§.§l[A-Za-z ]+:.*$");

    private static WynnObjective guildWynnObjective = null;

    private static final List<WynnObjective> WYNN_OBJECTIVES = new ArrayList<>();

    private static void tryRemoveObjective(WynnObjective parsed) {
        if (parsed.getGoal() == null) {
            return;
        }

        if (guildWynnObjective != null && guildWynnObjective.isSameObjective(parsed)) {
            guildWynnObjective = null;
            return;
        }

        WYNN_OBJECTIVES.removeIf(wynnObjective -> wynnObjective.isSameObjective(parsed));
    }

    private static void updateObjective(WynnObjective parsed) {
        Optional<WynnObjective> objective = WYNN_OBJECTIVES.stream()
                .filter(wynnObjective -> wynnObjective.isSameObjective(parsed))
                .findFirst();

        if (objective.isEmpty()) {
            // New objective
            WYNN_OBJECTIVES.add(parsed);
        } else {
            // Objective progress updated
            objective.get().setScore(parsed.getScore());
        }

        if (WYNN_OBJECTIVES.size() > 3) {
            WynntilsMod.error("ObjectiveManager: Stored more than 3 objectives. Reset objective list.");
            WYNN_OBJECTIVES.clear();
            WYNN_OBJECTIVES.add(parsed);
        }
    }

    private static void updateGuildObjective(WynnObjective parsed) {
        if (guildWynnObjective != null && guildWynnObjective.isSameObjective(parsed)) {
            // Objective progress updated
            guildWynnObjective.setScore(parsed.getScore());
            return;
        }

        // New objective
        guildWynnObjective = parsed;
    }

    @Override
    public void resetHandler() {
        guildWynnObjective = null;
        WYNN_OBJECTIVES.clear();
    }

    public static WynnObjective getGuildObjective() {
        return guildWynnObjective;
    }

    public static List<WynnObjective> getObjectives() {
        // Make copy, so we don't have to worry about concurrent modification
        return new ArrayList<>(WYNN_OBJECTIVES);
    }

    @Override
    public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
        List<WynnObjective> objectives = reparseObjectives(newValue).stream()
                .filter(wynnObjective -> wynnObjective.getScore() < wynnObjective.getMaxScore())
                .toList();

        if (segmentType == ScoreboardModel.SegmentType.GuildObjective) {
            for (WynnObjective objective : objectives) {
                if (objective.isGuildObjective()) {
                    updateGuildObjective(objective);
                }
            }
        } else {
            for (WynnObjective objective : objectives) {
                if (!objective.isGuildObjective()) {
                    updateObjective(objective);
                }
            }

            // filter out deleted objectives
            WYNN_OBJECTIVES.removeIf(
                    wynnObjective -> objectives.stream().noneMatch(other -> other.isSameObjective(wynnObjective)));
        }
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
        List<WynnObjective> objectives = reparseObjectives(segment);

        for (WynnObjective objective : objectives) {
            tryRemoveObjective(objective);
        }
    }

    private List<WynnObjective> reparseObjectives(Segment segment) {
        List<WynnObjective> parsedObjectives = new ArrayList<>();

        List<String> actualContent = new ArrayList<>();
        StringBuilder multiLine = new StringBuilder();

        for (String line : segment.getContent()) {
            if (OBJECTIVE_PATTERN_ONE_LINE.matcher(line).matches()) {
                actualContent.add(line);
                continue;
            }

            if (OBJECTIVE_PATTERN_MULTILINE_START.matcher(line).matches()) {
                if (!multiLine.isEmpty()) {
                    WynntilsMod.error("ObjectiveManager: Multi-line objective start repeatedly:");
                    WynntilsMod.error("Already got: " + multiLine);
                    WynntilsMod.error("Next line: " + line);
                }

                multiLine = new StringBuilder(line);
                continue;
            }

            // If we have started collecting a multiline, keep building it
            if (!multiLine.isEmpty()) {
                multiLine.append(line);
            }

            if (OBJECTIVE_PATTERN_MULTILINE_END.matcher(line).matches()) {
                actualContent.add(multiLine.toString().trim().replaceAll(" +", " "));
                multiLine = new StringBuilder();
            }
        }

        if (!multiLine.isEmpty() && !SEGMENT_HEADER.matcher(multiLine).matches()) {
            WynntilsMod.error("ObjectiveManager: Got a not finished multi-line objective: " + multiLine);
        }

        for (String line : actualContent) {
            Matcher objectiveMatcher = OBJECTIVE_PATTERN_ONE_LINE.matcher(line);
            if (!objectiveMatcher.matches()) {
                WynntilsMod.error("ObjectiveManager: Broken objective stored: " + line);
                continue;
            }

            // Determine objective type with the formatting code
            boolean isGuildObjective = Objects.equals(objectiveMatcher.group(1), "b");
            WynnObjective parsed = WynnObjective.parseObjectiveLine(line, isGuildObjective);

            parsedObjectives.add(parsed);
        }
        return parsedObjectives;
    }
}
