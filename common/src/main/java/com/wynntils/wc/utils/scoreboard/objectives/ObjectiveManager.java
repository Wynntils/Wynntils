/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.objectives;

import com.wynntils.core.WynntilsMod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectiveManager {
    // §b is guild objective, §a is normal objective and §c is daily objective
    public static final Pattern OBJECTIVE_PATTERN = Pattern.compile("^§([b|ac])[- ]\\s§7(.*): *§f(\\d+)§7/(\\d+)$");

    private static Objective guildObjective = null;

    private static final List<Objective> objectives = new ArrayList<>();

    public static void tryUpdateObjective(String objectiveString) {
        Matcher objectiveMatcher = OBJECTIVE_PATTERN.matcher(objectiveString);
        if (!objectiveMatcher.matches()) return;

        if (Objects.equals(objectiveMatcher.group(1), "b")) {
            updateGuildObjective(objectiveString);
        } else {
            updateObjective(objectiveString);
        }
    }

    public static void tryRemoveObjective(String objectiveString) {
        Matcher objectiveMatcher = OBJECTIVE_PATTERN.matcher(objectiveString);
        if (!objectiveMatcher.matches()) return;

        Objective parsed = Objective.parseObjectiveLine(objectiveString);

        if (guildObjective.isSameObjective(parsed)) {
            guildObjective = null;
            return;
        }

        objectives.removeIf(objective -> objective.isSameObjective(parsed));
    }

    private static void updateObjective(String objectiveString) {
        Objective parsed = Objective.parseObjectiveLine(objectiveString);

        objectives.removeIf(objective -> Objects.equals(objective.getGoal(), parsed.getGoal()));
        objectives.add(parsed);

        if (objectives.size() > 3) {
            WynntilsMod.error("ObjectiveManager: Stored more than 3 objectives. Reset objective list.");
            objectives.clear();
            objectives.add(parsed);
        }
    }

    private static void updateGuildObjective(String objectiveString) {
        guildObjective = Objective.parseObjectiveLine(objectiveString);
    }

    public static void resetObjectives() {
        guildObjective = null;
        objectives.clear();
    }

    public static Objective getGuildObjective() {
        return guildObjective;
    }

    public static List<Objective> getObjectives() {
        return objectives;
    }
}
