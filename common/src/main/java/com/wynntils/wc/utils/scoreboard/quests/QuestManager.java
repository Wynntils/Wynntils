/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wc.utils.scoreboard.ScoreboardHandler;
import com.wynntils.wc.utils.scoreboard.ScoreboardManager;
import com.wynntils.wc.utils.scoreboard.Segment;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;

public class QuestManager implements ScoreboardHandler {
    private static QuestInfo currentQuest = null;

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }

    @Override
    public void onSegmentChange(Segment newValue, ScoreboardManager.SegmentType segmentType) {
        List<String> content = newValue.getContent();

        List<String> questLines = content.stream()
                .map(ChatFormatting::stripFormatting)
                .filter(Objects::nonNull)
                .toList();

        if (questLines.isEmpty()) {
            WynntilsMod.error("QuestManager: questLines was empty.");
        }

        currentQuest =
                new QuestInfo(questLines.get(0), questLines.stream().skip(1).collect(Collectors.joining(" ")));
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardManager.SegmentType segmentType) {
        resetCurrentQuest();
    }

    public static void resetCurrentQuest() {
        currentQuest = null;
    }
}
