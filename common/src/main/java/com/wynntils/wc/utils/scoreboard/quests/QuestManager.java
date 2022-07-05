/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.WynntilsScoreboardUpdateEvent;
import com.wynntils.wc.utils.scoreboard.ScoreboardLine;
import com.wynntils.wc.utils.scoreboard.ScoreboardManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

public class QuestManager {

    private static final String TRACKED_QUEST_STRING = "§6§lTracked Quest:";

    private static QuestInfo currentQuest = null;

    @SubscribeEvent
    public static void onScoreboardUpdate(WynntilsScoreboardUpdateEvent event) {
        if (!event.getChangeMap().containsKey(WynntilsScoreboardUpdateEvent.ChangeType.Quest)) return;

        Set<WynntilsScoreboardUpdateEvent.Change> changes =
                event.getChangeMap().get(WynntilsScoreboardUpdateEvent.ChangeType.Quest);

        for (WynntilsScoreboardUpdateEvent.Change change : changes) {
            if (change.method() == ServerScoreboard.Method.REMOVE) {
                if (TRACKED_QUEST_STRING.equals(change.line())) {
                    questTrackingStopped();
                }
            } else { // Method is CHANGE
                // Use copy constructor to prevent threading issues
                List<ScoreboardLine> reconstructedScoreboard =
                        new ArrayList<>(ScoreboardManager.getReconstructedScoreboard());

                List<String> questLines = reconstructedScoreboard.stream()
                        .dropWhile(scoreboardLine -> !scoreboardLine.line().equals(TRACKED_QUEST_STRING))
                        .takeWhile(scoreboardLine -> !scoreboardLine.line().matches("À+"))
                        .map(scoreboardLine -> ChatFormatting.stripFormatting(scoreboardLine.line()))
                        .filter(Objects::nonNull)
                        .skip(1)
                        .toList();

                if (questLines.isEmpty()) {
                    WynntilsMod.error("QuestManager: questLines was empty.");
                }

                currentQuest = new QuestInfo(
                        questLines.get(0),
                        StringUtils.joinWith(" ", questLines.stream().skip(1).collect(Collectors.toList())));

                System.out.println(currentQuest);

                // Only update currentQuest once per batch.
                break;
            }
        }
    }

    public static void questTrackingStopped() {
        currentQuest = null;

        System.out.println(currentQuest);
    }

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }
}
