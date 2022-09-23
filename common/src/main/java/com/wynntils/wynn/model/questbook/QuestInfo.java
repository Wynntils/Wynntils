/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.questbook;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.Pair;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.objects.ProfessionInfo;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

public class QuestInfo {
    private static final int NEXT_TASK_MAX_WIDTH = 200;
    private static final Pattern QUEST_NAME_MATCHER = Pattern.compile("^§.§l([^֎À]*)[֎À]+ (§e\\[Tracked\\])?$");
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

    private final int pageNumber;
    private boolean tracked;

    public QuestInfo(
            String name,
            QuestStatus status,
            QuestLength length,
            int level,
            String nextTask,
            List<Pair<String, Integer>> additionalRequirements,
            int pageNumber,
            boolean tracked) {
        this.name = name;
        this.status = status;
        this.length = length;
        this.level = level;
        this.nextTask = nextTask;
        this.additionalRequirements = additionalRequirements;
        this.pageNumber = pageNumber;
        this.tracked = tracked;
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

    public int getPageNumber() {
        return pageNumber;
    }

    public boolean isTracked() {
        return tracked;
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

    public static List<Component> getTooltipLinesForQuest(QuestInfo questInfo) {
        List<Component> tooltipLines = new ArrayList<>();

        tooltipLines.add(new TextComponent(questInfo.getName())
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.WHITE));
        tooltipLines.add(questInfo.getStatus().getQuestBookComponent());
        tooltipLines.add(new TextComponent(""));
        tooltipLines.add((CharacterManager.getCharacterInfo().getLevel() >= questInfo.getLevel()
                        ? new TextComponent("✔").withStyle(ChatFormatting.GREEN)
                        : new TextComponent("✖").withStyle(ChatFormatting.RED))
                .append(new TextComponent(" Combat Lv. Min: ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(String.valueOf(questInfo.getLevel())).withStyle(ChatFormatting.WHITE)));

        for (Pair<String, Integer> additionalRequirement : questInfo.getAdditionalRequirements()) {
            MutableComponent base = CharacterManager.getCharacterInfo()
                                    .getProfessionInfo()
                                    .getLevel(ProfessionInfo.ProfessionType.valueOf(additionalRequirement.a))
                            >= additionalRequirement.b
                    ? new TextComponent("✔ ").withStyle(ChatFormatting.GREEN)
                    : new TextComponent("✖ ").withStyle(ChatFormatting.RED);

            tooltipLines.add(base.append(new TextComponent(additionalRequirement.a + " Lv. Min: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(String.valueOf(additionalRequirement.b))
                            .withStyle(ChatFormatting.WHITE))));
        }

        tooltipLines.add(new TextComponent("-")
                .withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(" Length: ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(StringUtils.capitalizeFirst(
                                questInfo.getLength().toString().toLowerCase(Locale.ROOT)))
                        .withStyle(ChatFormatting.WHITE)));

        if (questInfo.getStatus() != QuestStatus.COMPLETED) {
            tooltipLines.add(new TextComponent(""));
            String nextTask = questInfo.getNextTask();
            String[] lines = StringUtils.wrapTextBySize(nextTask, NEXT_TASK_MAX_WIDTH);

            for (String line : lines) {
                tooltipLines.add(new TextComponent(line).withStyle(ChatFormatting.GRAY));
            }
        }

        return tooltipLines;
    }

    public static QuestInfo parseItem(ItemStack item, int pageNumber) {
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
            boolean tracked = isQuestTracked(item);

            QuestInfo questInfo = new QuestInfo(
                    name, status, questLength, level, description, additionalRequirements, pageNumber, tracked);
            return questInfo;
        } catch (NoSuchElementException e) {
            WynntilsMod.warn("Failed to parse quest book item: " + item);
            return null;
        }
    }

    public static String getQuestName(ItemStack item) {
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

    private static boolean isQuestTracked(ItemStack item) {
        String rawName = item.getHoverName().getString();
        if (rawName.trim().isEmpty()) {
            return false;
        }
        return rawName.endsWith("§e[Tracked]");
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
                        descriptionLines.stream()
                                .map(line -> ChatFormatting.stripFormatting(line))
                                .toList())
                .replaceAll("\\s+", " ")
                .trim();
        return description;
    }
}
