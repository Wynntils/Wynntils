/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.TrackedQuestUpdateEvent;
import com.wynntils.wynn.model.scoreboard.ScoreboardHandler;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import com.wynntils.wynn.model.scoreboard.Segment;
import java.util.List;
import net.minecraft.ChatFormatting;

public class QuestHandler implements ScoreboardHandler {
    private static ScoreboardQuestInfo currentQuest = null;

    public static ScoreboardQuestInfo getCurrentQuest() {
        return currentQuest;
    }

    @Override
    public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
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

        setQuest(new ScoreboardQuestInfo(questName.toString().trim(), descriptionTrimmed));
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
        resetCurrentQuest();
    }

    public static void resetCurrentQuest() {
        setQuest(null);
    }

    private static void setQuest(ScoreboardQuestInfo questInfo) {
        currentQuest = questInfo;
        WynntilsMod.postEvent(new TrackedQuestUpdateEvent(currentQuest));
    }
}
