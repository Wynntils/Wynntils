/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.components.Models;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.quests.type.QuestLength;
import com.wynntils.models.quests.type.QuestStatus;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class QuestInfo {
    private static final int NEXT_TASK_MAX_WIDTH = 200;
    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile(".*\\[(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?\\].*");

    // Quest metadata is forever constant
    private final String name;
    private final QuestLength length;
    private final int level;
    /** Additional requirements as pairs of <"profession name", minLevel> */
    private final List<Pair<String, Integer>> additionalRequirements;

    private final boolean isMiniQuest;
    private final int pageNumber;

    // Quest progress can change over time
    private final QuestStatus status;
    private String nextTask;
    private final boolean tracked;

    protected QuestInfo(
            String name,
            QuestStatus status,
            QuestLength length,
            int level,
            String nextTask,
            List<Pair<String, Integer>> additionalRequirements,
            boolean isMiniQuest,
            int pageNumber,
            boolean tracked) {
        this.name = name;
        this.status = status;
        this.length = length;
        this.level = level;
        this.nextTask = nextTask;
        this.additionalRequirements = additionalRequirements;
        this.isMiniQuest = isMiniQuest;
        this.pageNumber = pageNumber;
        this.tracked = tracked;
    }

    public String getName() {
        return name;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public boolean isTrackable() {
        return status == QuestStatus.CAN_START || status == QuestStatus.STARTED;
    }

    public Optional<Location> getNextLocation() {
        Matcher matcher = COORDINATE_PATTERN.matcher(ComponentUtils.stripFormatting(nextTask));
        if (!matcher.matches()) return Optional.empty();

        return Optional.of(new Location(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))));
    }

    public QuestLength getLength() {
        return length;
    }

    public int getLevel() {
        return level;
    }

    public int getSortLevel() {
        return !isMiniQuest || additionalRequirements.isEmpty()
                ? level
                : additionalRequirements.get(0).b();
    }

    public String getNextTask() {
        return nextTask;
    }

    public void setNextTask(String nextTask) {
        this.nextTask = nextTask;
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

    public boolean isMiniQuest() {
        return isMiniQuest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestInfo questInfo = (QuestInfo) o;
        return level == questInfo.level
                && isMiniQuest == questInfo.isMiniQuest
                && Objects.equals(name, questInfo.name)
                && length == questInfo.length
                && Objects.equals(additionalRequirements, questInfo.additionalRequirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, length, level, additionalRequirements, isMiniQuest);
    }

    @Override
    public String toString() {
        return "QuestInfo[" + "name=\""
                + name + "\", " + "isMiniQuest="
                + isMiniQuest + ", " + "status="
                + status + ", " + "length="
                + length + ", " + "minLevel="
                + level + ", " + "nextTask=\""
                + nextTask + "\", " + "additionalRequirements="
                + additionalRequirements + "]";
    }

    public static List<Component> generateTooltipForQuest(QuestInfo questInfo) {
        List<Component> tooltipLines = new ArrayList<>();

        tooltipLines.add(Component.literal(questInfo.getName())
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.WHITE));
        tooltipLines.add(questInfo.getStatus().getQuestBookComponent());
        tooltipLines.add(Component.literal(""));
        // We always parse level as one, so check if this mini-quest does not have a min combat level
        if (!questInfo.isMiniQuest || questInfo.additionalRequirements.isEmpty()) {
            tooltipLines.add((Models.CombatXp.getCombatLevel().current() >= questInfo.getLevel()
                            ? Component.literal("✔").withStyle(ChatFormatting.GREEN)
                            : Component.literal("✖").withStyle(ChatFormatting.RED))
                    .append(Component.literal(" Combat Lv. Min: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(questInfo.getLevel()))
                            .withStyle(ChatFormatting.WHITE)));
        }

        for (Pair<String, Integer> additionalRequirement : questInfo.getAdditionalRequirements()) {
            MutableComponent base = Models.Profession.getLevel(ProfessionType.fromString(additionalRequirement.a()))
                            >= additionalRequirement.b()
                    ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                    : Component.literal("✖ ").withStyle(ChatFormatting.RED);

            tooltipLines.add(base.append(Component.literal(additionalRequirement.a() + " Lv. Min: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(additionalRequirement.b()))
                            .withStyle(ChatFormatting.WHITE))));
        }

        tooltipLines.add(Component.literal("-")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" Length: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtils.capitalizeFirst(
                                questInfo.getLength().toString().toLowerCase(Locale.ROOT)))
                        .withStyle(ChatFormatting.WHITE)));

        if (questInfo.getStatus() != QuestStatus.COMPLETED) {
            tooltipLines.add(Component.literal(""));
            String nextTask = questInfo.getNextTask();
            String[] lines = RenderedStringUtils.wrapTextBySize(nextTask, NEXT_TASK_MAX_WIDTH);

            for (String line : lines) {
                tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }

        return tooltipLines;
    }
}
