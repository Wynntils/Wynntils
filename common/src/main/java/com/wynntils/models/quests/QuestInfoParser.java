/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText2;
import com.wynntils.models.quests.type.QuestLength;
import com.wynntils.models.quests.type.QuestStatus;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class QuestInfoParser {
    private static final Pattern QUEST_NAME_MATCHER =
            Pattern.compile("^§.§l(Mini-Quest - )?([^֎À]*)[֎À]+ (§e\\[Tracked\\])?$");
    private static final Pattern STATUS_MATCHER = Pattern.compile("^§.(.*)(?:\\.\\.\\.|!)$");
    private static final Pattern LENGTH_MATCHER = Pattern.compile("^§a-§r§7 Length: §r§f(.*)$");
    private static final Pattern LEVEL_MATCHER = Pattern.compile("^§..§r§7 Combat Lv. Min: §r§f(\\d+)$");
    private static final Pattern REQ_MATCHER = Pattern.compile("^§..§r§7 (.*) Lv. Min: §r§f(\\d+)$");

    static QuestInfo parseItemStack(ItemStack itemStack, int pageNumber, boolean isMiniQuest) {
        try {
            String name = getQuestName(itemStack);
            if (name == null) return null;

            LinkedList<StyledText2> lore = LoreUtils.getLore(itemStack);

            QuestStatus status = getQuestStatus(lore);
            if (status == null) return null;

            if (!skipEmptyLine(lore)) return null;

            int level = getLevel(lore);
            List<Pair<String, Integer>> additionalRequirements = getAdditionalRequirements(lore);

            QuestLength questLength = getQuestLength(lore);
            if (questLength == null) return null;

            if (!skipEmptyLine(lore)) return null;

            StyledText2 description = getDescription(lore);
            boolean tracked = isQuestTracked(itemStack);

            return new QuestInfo(
                    name,
                    status,
                    questLength,
                    level,
                    description,
                    additionalRequirements,
                    isMiniQuest,
                    pageNumber,
                    tracked);
        } catch (NoSuchElementException e) {
            WynntilsMod.warn("Failed to parse quest book item: " + itemStack);
            return null;
        }
    }

    static String getQuestName(ItemStack itemStack) {
        String rawName = itemStack.getHoverName().getString();
        if (rawName.trim().isEmpty()) {
            return null;
        }
        Matcher m = QUEST_NAME_MATCHER.matcher(rawName);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching quest name: " + rawName);
            return null;
        }
        return m.group(2);
    }

    private static boolean isQuestTracked(ItemStack itemStack) {
        StyledText2 name =
                StyledText2.fromComponentIgnoringComponentStylesAndJustUsingFormattingCodes(itemStack.getHoverName());
        if (name.trim().isEmpty()) {
            return false;
        }
        return name.endsWith("§e[Tracked]");
    }

    private static QuestStatus getQuestStatus(LinkedList<StyledText2> lore) {
        StyledText2 rawStatus = lore.pop();
        Matcher m = rawStatus.getMatcher(STATUS_MATCHER);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching status value: " + rawStatus);
            return null;
        }
        return QuestStatus.fromString(m.group(1));
    }

    private static boolean skipEmptyLine(LinkedList<StyledText2> lore) {
        StyledText2 loreLine = lore.pop();
        if (!loreLine.isEmpty()) {
            WynntilsMod.warn("Unexpected value in quest: " + loreLine);
            return false;
        }
        return true;
    }

    private static int getLevel(LinkedList<StyledText2> lore) {
        StyledText2 rawLevel = lore.getFirst();
        Matcher m = rawLevel.getMatcher(LEVEL_MATCHER);
        if (!m.find()) {
            // This can happen for the very first quests; accept without error
            // and interpret level requirement as 1
            return 1;
        }
        lore.pop();
        return Integer.parseInt(m.group(1));
    }

    private static List<Pair<String, Integer>> getAdditionalRequirements(LinkedList<StyledText2> lore) {
        List<Pair<String, Integer>> requirements = new LinkedList<>();
        Matcher m;

        m = lore.getFirst().getMatcher(REQ_MATCHER);
        while (m.matches()) {
            lore.pop();
            String profession = m.group(1);
            int level = Integer.parseInt(m.group(2));
            Pair<String, Integer> requirement = new Pair<>(profession, level);
            requirements.add(requirement);

            m = lore.getFirst().getMatcher(REQ_MATCHER);
        }
        return requirements;
    }

    private static QuestLength getQuestLength(LinkedList<StyledText2> lore) {
        StyledText2 lengthRaw = lore.pop();

        Matcher m = lengthRaw.getMatcher(LENGTH_MATCHER);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching quest length: " + lengthRaw);
            return null;
        }
        return QuestLength.fromString(m.group(1));
    }

    private static StyledText2 getDescription(List<StyledText2> lore) {
        // The last two lines is an empty line and "RIGHT-CLICK TO TRACK"; skip those
        List<StyledText2> descriptionLines = lore.subList(0, lore.size() - 2);
        // Every line begins with a format code of length 2 ("§7"), skip that
        // and join everything together, trying to avoid excess whitespace

        // FIXME: We should really keep the rest of the formatting, apart from the
        // initial §7.

        String description = String.join(
                        " ",
                        descriptionLines.stream()
                                .map(StyledText2::getUnformattedString)
                                .toList())
                .replaceAll("\\s+", " ")
                .trim();
        return StyledText2.fromString(description);
    }
}
