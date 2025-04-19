/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ObjectivesModel extends Model {
    private static final ScoreboardPart DAILY_OBJECTIVES_SCOREBOARD_PART = new DailyObjectiveScoreboardPart();
    private static final ScoreboardPart GUILD_OBJECTIVES_SCOREBOARD_PART = new GuildObjectiveScoreboardPart();

    private List<WynnObjective> personalObjectives = new ArrayList<>();
    private WynnObjective guildObjective = null;

    public ObjectivesModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(DAILY_OBJECTIVES_SCOREBOARD_PART);
        Handlers.Scoreboard.addPart(GUILD_OBJECTIVES_SCOREBOARD_PART);
    }

    public WynnObjective getGuildObjective() {
        return guildObjective;
    }

    public List<WynnObjective> getPersonalObjectives() {
        // Make copy, so we don't have to worry about concurrent modification
        return Collections.unmodifiableList(personalObjectives);
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
            objective.get().setCurrentScore(parsed.getScore().current());
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
            guildObjective.setCurrentScore(parsed.getScore().current());
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
