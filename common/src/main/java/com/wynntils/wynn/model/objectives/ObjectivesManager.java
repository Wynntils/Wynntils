/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.handlers.scoreboard.ScoreboardListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObjectivesManager extends Manager {
    public static final ScoreboardListener SCOREBOARD_LISTENER = new ObjectiveListener();

    private List<WynnObjective> personalObjectives = new ArrayList<>();
    private WynnObjective guildObjective = null;

    public ObjectivesManager() {
        super(List.of());
    }

    public WynnObjective getGuildObjective() {
        return guildObjective;
    }

    public List<WynnObjective> getPersonalObjectives() {
        // Make copy, so we don't have to worry about concurrent modification
        return new ArrayList<>(personalObjectives);
    }

    void resetObjectives() {
        guildObjective = null;
        personalObjectives = new ArrayList<>();
    }

    void updatePersonalObjective(WynnObjective parsed) {
        Optional<WynnObjective> objective = personalObjectives.stream()
                .filter(wynnObjective -> wynnObjective.isSameObjective(parsed))
                .findFirst();

        if (objective.isEmpty()) {
            // New objective
            personalObjectives.add(parsed);
        } else {
            // Objective progress updated
            objective.get().setScore(parsed.getScore());
        }

        if (personalObjectives.size() > 3) {
            WynntilsMod.error("ObjectiveManager: Stored more than 3 objectives. Reset objective list.");
            personalObjectives.clear();
            personalObjectives.add(parsed);
        }
    }

    void purgePersonalObjectives(List<WynnObjective> objectives) {
        personalObjectives.removeIf(
                wynnObjective -> objectives.stream().noneMatch(other -> other.isSameObjective(wynnObjective)));
    }

    void updateGuildObjective(WynnObjective parsed) {
        if (guildObjective != null && guildObjective.isSameObjective(parsed)) {
            // Objective progress updated
            guildObjective.setScore(parsed.getScore());
            return;
        }

        // New objective
        guildObjective = parsed;
    }

    void removeObjective(WynnObjective parsed) {
        if (guildObjective != null && guildObjective.isSameObjective(parsed)) {
            guildObjective = null;
            return;
        }

        personalObjectives.removeIf(wynnObjective -> wynnObjective.isSameObjective(parsed));
    }
}
