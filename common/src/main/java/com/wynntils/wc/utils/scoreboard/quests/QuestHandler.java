/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wc.utils.scoreboard.ScoreboardHandler;
import com.wynntils.wc.utils.scoreboard.ScoreboardManager;
import com.wynntils.wc.utils.scoreboard.Segment;
import java.util.List;
import net.minecraft.ChatFormatting;

public class QuestHandler implements ScoreboardHandler {
    private static QuestInfo currentQuest = null;

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }

    @Override
    public void onSegmentChange(Segment newValue, ScoreboardManager.SegmentType segmentType) {
        List<String> content = newValue.getContent();

        if (content.isEmpty()) {
            WynntilsMod.error("QuestHandler: content was empty.");
        }

        StringBuilder questName = new StringBuilder();
        StringBuilder description = new StringBuilder();

        for (String line : content) {
            if (line.startsWith("§e")) {
                questName.append(ComponentUtils.stripFormatting(line)).append(" ");
            } else {
                description
                        .append(line.replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                                .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()))
                        .append(" ");
            }
        }

        String descriptionTrimmed = description.toString().trim();

        currentQuest = new QuestInfo(questName.toString().trim(), descriptionTrimmed);
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardManager.SegmentType segmentType) {
        resetCurrentQuest();
    }

    public static void resetCurrentQuest() {
        currentQuest = null;
    }
}
