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
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class QuestManager implements ScoreboardHandler {
    private static final Pattern COORDINATE_BRACKET_PATTERN = Pattern.compile("\\[-?\\d+,-?\\d+,-?\\d+\\]");

    private static QuestInfo currentQuest = null;

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }

    @Override
    public void onSegmentChange(Segment newValue, ScoreboardManager.SegmentType segmentType) {
        List<String> content = newValue.getContent();

        if (content.isEmpty()) {
            WynntilsMod.error("QuestManager: content was empty.");
        }

        StringBuilder questName = new StringBuilder();
        StringBuilder description = new StringBuilder();

        for (String line : content) {
            if (line.startsWith("§e")) {
                questName.append(ComponentUtils.stripFormatting(line)).append(" ");
            } else {
                description.append(ComponentUtils.stripFormatting(line)).append(" ");
            }
        }

        String descriptionTrimmed = description.toString().trim();

        StringBuilder formattedDescriptionBuilder = new StringBuilder();

        int openingBracket = descriptionTrimmed.indexOf('[');
        int closingBracket = descriptionTrimmed.indexOf(']', openingBracket);

        if (openingBracket != -1 && closingBracket != -1) {
            formattedDescriptionBuilder.append(descriptionTrimmed, 0, openingBracket);

            while (openingBracket != -1 && closingBracket != -1) {
                String bracketPart = descriptionTrimmed.substring(openingBracket, closingBracket + 1);

                ChatFormatting color =
                        COORDINATE_BRACKET_PATTERN.matcher(bracketPart).matches()
                                ? ChatFormatting.LIGHT_PURPLE
                                : ChatFormatting.DARK_AQUA;
                formattedDescriptionBuilder.append(color).append(bracketPart);

                openingBracket = descriptionTrimmed.indexOf('[', closingBracket);

                if (openingBracket != -1) {
                    formattedDescriptionBuilder
                            .append(ChatFormatting.RESET)
                            .append(descriptionTrimmed, closingBracket + 1, openingBracket);
                }

                closingBracket = descriptionTrimmed.indexOf(']', openingBracket);
            }

            int lastClosing = descriptionTrimmed.lastIndexOf(']');

            if (lastClosing != -1) {
                formattedDescriptionBuilder
                        .append(ChatFormatting.RESET)
                        .append(descriptionTrimmed.substring(lastClosing + 1));
            }
        } else {
            formattedDescriptionBuilder = new StringBuilder(descriptionTrimmed);
        }

        currentQuest = new QuestInfo(questName.toString().trim(), formattedDescriptionBuilder.toString());
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardManager.SegmentType segmentType) {
        resetCurrentQuest();
    }

    public static void resetCurrentQuest() {
        currentQuest = null;
    }
}
