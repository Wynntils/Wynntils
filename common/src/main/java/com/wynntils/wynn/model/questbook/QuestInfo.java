/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.questbook;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.Pair;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class QuestInfo {
    private static final Pattern QUEST_NAME_MATCHER = Pattern.compile("^§.§l([^֎À]*)[֎À]+ $");
    private static final Pattern STATUS_MATCHER = Pattern.compile("^§.(.*)(?:\\.\\.\\.|!)$");
    private static final Pattern LENGTH_MATCHER = Pattern.compile("^§a-§r§7 Length: §r§f(.*)$");
    private static final Pattern LEVEL_MATCHER = Pattern.compile("^§..§r§7 Combat Lv. Min: §r§f(\\d+)$");
    private static final Pattern REQ_MATCHER = Pattern.compile("^§..§r§7 (.*) Lv. Min: §r§f(\\d+)$");

    private final String name;
    private final QuestStatus status;
    private final QuestLength length;
    private final int level;
    private final String nextTask;
    /** Additional requirements as pairs of <"profession name", minLevel> */
    private final List<Pair<String, Integer>> additionalRequirements;

    public QuestInfo(
            String name,
            QuestStatus status,
            QuestLength length,
            int level,
            String nextTask,
            List<Pair<String, Integer>> additionalRequirements) {
        this.name = name;
        this.status = status;
        this.length = length;
        this.level = level;
        this.nextTask = nextTask;
        this.additionalRequirements = additionalRequirements;
    }

    public String getName() {
        return name;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public QuestLength getLength() {
        return length;
    }

    public int getLevel() {
        return level;
    }

    public String getNextTask() {
        return nextTask;
    }

    public List<Pair<String, Integer>> getAdditionalRequirements() {
        return additionalRequirements;
    }

    @Override
    public String toString() {
        return "QuestInfo[" + "name=\""
                + name + "\", " + "status="
                + status + ", " + "length="
                + length + ", " + "minLevel="
                + level + ", " + "nextTask=\""
                + nextTask + "\", " + "additionalRequirements="
                + additionalRequirements + "]";
    }

    public static QuestInfo parseItem(ItemStack item) {
        try {
            String name = getQuestName(item);
            if (name == null) return null;

            LinkedList<String> lore = ItemUtils.getLore(item);

            QuestStatus status = getQuestStatus(lore);
            if (status == null) return null;

            if (!skipEmptyLine(lore)) return null;

            int level = getLevel(lore);
            List<Pair<String, Integer>> additionalRequirements = getAdditionalRequirements(lore);

            QuestLength questLength = getQuestLength(lore);
            if (questLength == null) return null;

            if (!skipEmptyLine(lore)) return null;

            String description = getDescription(lore);

            QuestInfo questInfo = new QuestInfo(name, status, questLength, level, description, additionalRequirements);
            return questInfo;
        } catch (NoSuchElementException e) {
            WynntilsMod.warn("Failed to parse quest book item: " + item);
            return null;
        }
    }

    private static String getQuestName(ItemStack item) {
        String rawName = item.getHoverName().getString();
        if (rawName.trim().isEmpty()) {
            return null;
        }
        Matcher m = QUEST_NAME_MATCHER.matcher(rawName);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching quest name: " + rawName);
            return null;
        }
        return m.group(1);
    }

    private static QuestStatus getQuestStatus(LinkedList<String> lore) {
        String rawStatus = lore.pop();
        Matcher m = STATUS_MATCHER.matcher(rawStatus);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching status value: " + rawStatus);
            return null;
        }
        return QuestStatus.fromString(m.group(1));
    }

    private static boolean skipEmptyLine(LinkedList<String> lore) {
        String loreLine = lore.pop();
        if (!loreLine.isEmpty()) {
            WynntilsMod.warn("Unexpected value in quest: " + loreLine);
            return false;
        }
        return true;
    }

    private static int getLevel(LinkedList<String> lore) {
        String rawLevel = lore.getFirst();
        Matcher m = LEVEL_MATCHER.matcher(rawLevel);
        if (!m.find()) {
            // This can happen for the very first quests; accept without error
            // and interpret level requirement as 1
            return 1;
        }
        lore.pop();
        return Integer.parseInt(m.group(1));
    }

    private static List<Pair<String, Integer>> getAdditionalRequirements(LinkedList<String> lore) {
        List<Pair<String, Integer>> requirements = new LinkedList<>();
        Matcher m;

        m = REQ_MATCHER.matcher(lore.getFirst());
        while (m.matches()) {
            lore.pop();
            String profession = m.group(1);
            int level = Integer.parseInt(m.group(2));
            Pair<String, Integer> requirement = new Pair<>(profession, level);
            requirements.add(requirement);

            m = REQ_MATCHER.matcher(lore.getFirst());
        }
        return requirements;
    }

    private static QuestLength getQuestLength(LinkedList<String> lore) {
        String lengthRaw = lore.pop();

        Matcher m = LENGTH_MATCHER.matcher(lengthRaw);
        if (!m.find()) {
            WynntilsMod.warn("Non-matching quest length: " + lengthRaw);
            return null;
        }
        return QuestLength.fromString(m.group(1));
    }

    private static String getDescription(LinkedList<String> lore) {
        // The last two lines is an empty line and "RIGHT-CLICK TO TRACK"; skip those
        List<String> descriptionLines = lore.subList(0, lore.size() - 2);
        // Every line begins with a format code of length 2 ("§7"), skip that
        // and join everything together, trying to avoid excess whitespace
        String description = String.join(
                        " ",
                        descriptionLines.stream().map(line -> line.substring(2)).toList())
                .replaceAll("  ", " ")
                .trim();
        return description;
    }
}
