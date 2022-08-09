/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wc.utils.scoreboard.ScoreboardHandler;
import com.wynntils.wc.utils.scoreboard.ScoreboardManager;
import com.wynntils.wc.utils.scoreboard.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectiveManager implements ScoreboardHandler {
    // §b is guild objective, §a is normal objective and §c is daily objective
    private static final Pattern OBJECTIVE_PATTERN = Pattern.compile("^§([abc])[- ]\\s§7(.*): *§f(\\d+)§7/(\\d+)$");

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

    public static void resetObjectives() {
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
    public void onSegmentChange(Segment newValue, ScoreboardManager.SegmentType segmentType) {
        List<WynnObjective> objectives = reparseObjectives(newValue).stream()
                .filter(wynnObjective -> wynnObjective.getScore() != wynnObjective.getMaxScore())
                .toList();

        if (segmentType == ScoreboardManager.SegmentType.GuildObjective) {
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
    public void onSegmentRemove(Segment segment, ScoreboardManager.SegmentType segmentType) {
        List<WynnObjective> objectives = reparseObjectives(segment);

        for (WynnObjective objective : objectives) {
            tryRemoveObjective(objective);
        }
    }

    private List<WynnObjective> reparseObjectives(Segment segment) {
        List<WynnObjective> parsedObjectives = new ArrayList<>();
        for (String line : segment.getContent()) {
            Matcher objectiveMatcher = OBJECTIVE_PATTERN.matcher(line);
            if (!objectiveMatcher.matches()) {
                continue;
            }

            WynnObjective parsed = WynnObjective.parseObjectiveLine(line);

            // Determine objective type with the formatting code
            if (Objects.equals(objectiveMatcher.group(1), "b")) {
                parsed.setGuildObjective(true);
            }

            parsedObjectives.add(parsed);
        }
        return parsedObjectives;
    }
}
