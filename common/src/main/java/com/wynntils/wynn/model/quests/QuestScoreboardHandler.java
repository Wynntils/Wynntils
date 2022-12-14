/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.model.scoreboard.ScoreboardHandler;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import com.wynntils.wynn.model.scoreboard.Segment;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;

public class QuestScoreboardHandler implements ScoreboardHandler {
    @Override
    public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
        List<String> content = newValue.getContent();

        if (content.isEmpty()) {
            WynntilsMod.error("QuestHandler: content was empty.");
        }

        StringBuilder questName = new StringBuilder();
        StringBuilder nextTask = new StringBuilder();

        for (String line : content) {
            if (line.startsWith("§e")) {
                questName.append(ComponentUtils.stripFormatting(line)).append(" ");
            } else {
                nextTask.append(line.replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                                .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()))
                        .append(" ");
            }
        }

        Optional<QuestInfo> questInfoOpt = Managers.QUEST.getQuestFromName(
                WynnUtils.normalizeBadString(questName.toString().trim()));
        if (questInfoOpt.isEmpty()) {
            WynntilsMod.warn("Cannot match quest from scoreboard to actual quest: " + questName);
            return;
        }

        QuestInfo questInfo = questInfoOpt.get();
        questInfo.setNextTask(nextTask.toString().trim());

        Managers.QUEST.setCurrentQuest(questInfo);
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
        Managers.QUEST.setCurrentQuest(null);
    }

    @Override
    public void resetHandler() {
        Managers.QUEST.setCurrentQuest(null);
    }
}
