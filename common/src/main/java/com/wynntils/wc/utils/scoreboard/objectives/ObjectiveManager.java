/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.WynntilsScoreboardUpdateEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ObjectiveManager {
    // §b is guild objective, §a is normal objective and §c is daily objective
    public static final Pattern OBJECTIVE_PATTERN = Pattern.compile("^§([abc])[- ]\\s§7(.*): *§f(\\d+)§7/(\\d+)$");

    private static WynnObjective guildWynnObjective = null;

    private static final List<WynnObjective> WYNN_OBJECTIVES = new ArrayList<>();

    @SubscribeEvent
    public static void onScoreboardUpdate(WynntilsScoreboardUpdateEvent event) {
        if (!event.getChangeMap().containsKey(WynntilsScoreboardUpdateEvent.ChangeType.Objective)) return;

        Set<WynntilsScoreboardUpdateEvent.Change> changes =
                event.getChangeMap().get(WynntilsScoreboardUpdateEvent.ChangeType.Objective);

        for (WynntilsScoreboardUpdateEvent.Change change : changes) {
            Matcher objectiveMatcher = OBJECTIVE_PATTERN.matcher(change.line());
            if (!objectiveMatcher.matches()) {
                WynntilsMod.error("ObjectiveManager: '" + change.line() + "' did not match objective format.");
                continue;
            }

            WynnObjective parsed = WynnObjective.parseObjectiveLine(change.line());

            if (change.method() == ServerScoreboard.Method.CHANGE) {
                // Determine objective type with the formatting code
                if (Objects.equals(objectiveMatcher.group(1), "b")) {
                    updateGuildObjective(parsed);
                } else {
                    updateObjective(parsed);
                }
            } else { // Method is REMOVE
                tryRemoveObjective(parsed);
            }
        }
    }

    public static void tryRemoveObjective(WynnObjective parsed) {
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
        if (guildWynnObjective.isSameObjective(parsed)) {
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
        return WYNN_OBJECTIVES;
    }
}
