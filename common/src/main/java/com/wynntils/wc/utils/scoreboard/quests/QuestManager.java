/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.quests;

import com.wynntils.mc.mixin.accessors.ScoreboardAccessor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.StringUtils;

public class QuestManager {

    private static final String TRACKED_QUEST_STRING = "§6§lTracked Quest:";

    private static QuestInfo currentQuest = null;

    public static void tryParseQuest() {
        Scoreboard scoreboard = McUtils.player().getScoreboard();

        List<Objective> objectives = scoreboard.getObjectives().stream().toList();

        if (objectives.isEmpty()) {
            return;
        }

        final Objective objective = objectives.get(0);

        // use copy constructor to prevent concurrent modification
        Map<String, Map<Objective, Score>> playerScores =
                new HashMap<>(((ScoreboardAccessor) scoreboard).getPlayerScores());

        List<String> objectiveLines = playerScores.values().stream()
                .map(objectiveScoreMap -> objectiveScoreMap.get(objective))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Score::getScore).reversed())
                .map(Score::getOwner)
                .toList();

        List<String> questLines = objectiveLines.stream()
                .dropWhile(s -> !s.equals(TRACKED_QUEST_STRING))
                .takeWhile(s -> !s.matches("À+"))
                .skip(1)
                .toList();

        if (questLines.isEmpty()) {
            return;
        }

        // Invalidate wrong parsing due to scoreboard refresh
        if (!questLines.get(0).startsWith("§e")) {
            return;
        }

        // An objective will appear among the quest lines if scoreboard is refreshing
        for (int i = 1; i < questLines.size(); i++) {
            if (ObjectiveManager.OBJECTIVE_PATTERN.matcher(questLines.get(i)).matches()) {
                questLines = questLines.subList(0, i);
                break;
            }
        }

        questLines = questLines.stream()
                .map(ChatFormatting::stripFormatting)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        currentQuest = new QuestInfo(
                questLines.get(0),
                StringUtils.joinWith(" ", questLines.stream().skip(1).toArray()));
    }

    public static void checkIfTrackingStopped(String objectiveLine) {
        if (objectiveLine.equals(TRACKED_QUEST_STRING)) {
            currentQuest = null;
        }
    }

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }
}
