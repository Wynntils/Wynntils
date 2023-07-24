/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.quests;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class QuestInfo {
    private static final int NEXT_TASK_MAX_WIDTH = 200;

    // Quest metadata is forever constant
    private final String name;
    private final ActivityLength length;
    private final int level;
    /** Additional requirements as pairs of <"profession name", minLevel> */
    private final List<Pair<String, Integer>> additionalRequirements;

    private final boolean isMiniQuest;

    private final ActivityStatus status;
    private final StyledText nextTask;

    protected QuestInfo(
            String name,
            ActivityStatus status,
            ActivityLength length,
            int level,
            StyledText nextTask,
            List<Pair<String, Integer>> additionalRequirements,
            boolean isMiniQuest) {
        this.name = name;
        this.status = status;
        this.length = length;
        this.level = level;
        this.nextTask = nextTask;
        this.additionalRequirements = additionalRequirements;
        this.isMiniQuest = isMiniQuest;
    }

    public String getName() {
        return name;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public boolean isTrackable() {
        return status == ActivityStatus.AVAILABLE || status == ActivityStatus.STARTED;
    }

    public Optional<Location> getNextLocation() {
        return StyledTextUtils.extractLocation(nextTask);
    }

    public ActivityLength getLength() {
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

    public StyledText getNextTask() {
        return nextTask;
    }

    public List<Pair<String, Integer>> getAdditionalRequirements() {
        return additionalRequirements;
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
        tooltipLines.add(questInfo.getStatus().getQuestStateComponent());
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

        if (questInfo.getStatus() != ActivityStatus.COMPLETED) {
            tooltipLines.add(Component.literal(""));
            StyledText nextTask = questInfo.getNextTask();
            StyledText[] lines = RenderedStringUtils.wrapTextBySize(nextTask, NEXT_TASK_MAX_WIDTH);

            for (StyledText line : lines) {
                tooltipLines.add(line.getComponent().withStyle(ChatFormatting.GRAY));
            }
        }

        return tooltipLines;
    }
}
