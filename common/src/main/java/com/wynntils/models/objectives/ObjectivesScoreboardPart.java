/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.utils.mc.type.StyledText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectivesScoreboardPart implements ScoreboardPart {
    static final SegmentMatcher OBJECTIVES_MATCHER = SegmentMatcher.fromPattern("([★⭑] )?(Daily )?Objectives?:");
    static final SegmentMatcher GUILD_OBJECTIVES_MATCHER = SegmentMatcher.fromPattern("([★⭑] )?Guild Obj: (.+)");

    // §b is guild objective, §a is normal objective and §c is daily objective
    private static final Pattern OBJECTIVE_PATTERN_ONE_LINE =
            Pattern.compile("^§([abc])[- ]\\s§7(.*): *§f(\\d+)§7/(\\d+)$");
    private static final Pattern OBJECTIVE_PATTERN_MULTILINE_START = Pattern.compile("^§([abc])[- ]\\s§7(.*)$");
    private static final Pattern OBJECTIVE_PATTERN_MULTILINE_END = Pattern.compile(".*§f(\\d+)§7/(\\d+)$");
    private static final Pattern SEGMENT_HEADER = Pattern.compile("^§.§l[A-Za-z ]+:.*$");

    @Override
    public Set<SegmentMatcher> getSegmentMatchers() {
        return Set.of(OBJECTIVES_MATCHER, GUILD_OBJECTIVES_MATCHER);
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue, SegmentMatcher segmentMatcher) {
        List<WynnObjective> objectives = parseObjectives(newValue).stream()
                .filter(wynnObjective -> wynnObjective.getScore().current()
                        < wynnObjective.getScore().max())
                .toList();

        if (segmentMatcher == GUILD_OBJECTIVES_MATCHER) {
            for (WynnObjective objective : objectives) {
                if (objective.isGuildObjective()) {
                    Models.Objectives.updateGuildObjective(objective);
                }
            }
        } else {
            for (WynnObjective objective : objectives) {
                if (!objective.isGuildObjective()) {
                    Models.Objectives.updatePersonalObjective(objective);
                }
            }

            // filter out deleted objectives
            Models.Objectives.purgePersonalObjectives(objectives);
        }
    }

    private List<WynnObjective> parseObjectives(ScoreboardSegment segment) {
        List<WynnObjective> parsedObjectives = new ArrayList<>();

        List<StyledText> actualContent = new ArrayList<>();
        StringBuilder multiLine = new StringBuilder();

        for (StyledText line : segment.getContent()) {
            if (line.match(OBJECTIVE_PATTERN_ONE_LINE).matches()) {
                actualContent.add(line);
                continue;
            }

            if (line.match(OBJECTIVE_PATTERN_MULTILINE_START).matches()) {
                if (!multiLine.isEmpty()) {
                    WynntilsMod.error("ObjectiveManager: Multi-line objective start repeatedly:");
                    WynntilsMod.error("Already got: " + multiLine);
                    WynntilsMod.error("Next line: " + line);
                }

                multiLine = new StringBuilder(line.str());
                continue;
            }

            // If we have started collecting a multiline, keep building it
            if (!multiLine.isEmpty()) {
                multiLine.append(line);
            }

            if (line.match(OBJECTIVE_PATTERN_MULTILINE_END).matches()) {
                actualContent.add(StyledText.of(multiLine.toString().trim().replaceAll(" +", " ")));
                multiLine = new StringBuilder();
            }
        }

        if (!multiLine.isEmpty() && !SEGMENT_HEADER.matcher(multiLine).matches()) {
            WynntilsMod.error("ObjectiveManager: Got a not finished multi-line objective: " + multiLine);
        }

        for (StyledText line : actualContent) {
            Matcher objectiveMatcher = line.match(OBJECTIVE_PATTERN_ONE_LINE);
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

    @Override
    public void onSegmentRemove(ScoreboardSegment segment, SegmentMatcher segmentMatcher) {
        List<WynnObjective> objectives = parseObjectives(segment);

        for (WynnObjective objective : objectives) {
            if (objective.getGoal() != null) {
                Models.Objectives.removeObjective(objective);
            }
        }
    }

    @Override
    public void reset() {
        Models.Objectives.resetObjectives();
    }
}
