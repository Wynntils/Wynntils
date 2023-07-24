/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.quests;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRequirements;
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
    private final String specialInfo;
    private final ActivityLength length;
    private final ActivityDifficulty difficulty;
    private final int level;
    private final ActivityRequirements additionalRequirements;
    private final boolean isMiniQuest;
    private final List<String> rewards;

    private final ActivityStatus status;
    private final StyledText nextTask;

    protected QuestInfo(
            String name,
            String specialInfo,
            ActivityDifficulty difficulty,
            ActivityStatus status,
            ActivityLength length,
            int level,
            StyledText nextTask,
            ActivityRequirements additionalRequirements,
            boolean isMiniQuest,
            List<String> rewards) {
        this.name = name;
        this.specialInfo = specialInfo;
        this.difficulty = difficulty;
        this.status = status;
        this.length = length;
        this.level = level;
        this.nextTask = nextTask;
        this.additionalRequirements = additionalRequirements;
        this.isMiniQuest = isMiniQuest;
        this.rewards = rewards;
    }

    public String getName() {
        return name;
    }

    public String getSpecialInfo() {
        return specialInfo;
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

    public ActivityDifficulty getDifficulty() {
        return difficulty;
    }

    public int getLevel() {
        return level;
    }

    public int getSortLevel() {
        return !isMiniQuest || additionalRequirements.level().a() != 0
                ? level
                : additionalRequirements.professionLevels().get(0).a().b();
    }

    public StyledText getNextTask() {
        return nextTask;
    }

    public ActivityRequirements getAdditionalRequirements() {
        return additionalRequirements;
    }

    public boolean isMiniQuest() {
        return isMiniQuest;
    }

    public List<String> getRewards() {
        return rewards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestInfo questInfo = (QuestInfo) o;
        return level == questInfo.level
                && Objects.equals(specialInfo, questInfo.specialInfo)
                && isMiniQuest == questInfo.isMiniQuest
                && Objects.equals(name, questInfo.name)
                && length == questInfo.length
                && difficulty == questInfo.difficulty
                && Objects.equals(additionalRequirements, questInfo.additionalRequirements)
                && Objects.equals(rewards, questInfo.rewards)
                && status == questInfo.status
                && Objects.equals(nextTask, questInfo.nextTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                specialInfo,
                length,
                difficulty,
                level,
                additionalRequirements,
                isMiniQuest,
                rewards,
                status,
                nextTask);
    }

    @Override
    public String toString() {
        return "QuestInfo{" + "name='"
                + name + '\'' + ", specialInfo='"
                + specialInfo + '\'' + ", length="
                + length + ", difficulty="
                + difficulty + ", level="
                + level + ", additionalRequirements="
                + additionalRequirements + ", isMiniQuest="
                + isMiniQuest + ", rewards="
                + rewards + ", status="
                + status + ", nextTask="
                + nextTask + '}';
    }

    public static List<Component> generateTooltipForQuest(QuestInfo questInfo) {
        List<Component> tooltipLines = new ArrayList<>();

        tooltipLines.add(Component.literal(questInfo.getName())
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.WHITE));

        tooltipLines.add(questInfo.getStatus().getQuestStateComponent());
        if (questInfo.getSpecialInfo() != null) {
            tooltipLines.add(Component.literal(questInfo.getSpecialInfo()).withStyle(ChatFormatting.GREEN));
        }

        tooltipLines.add(Component.literal(""));
        // We always parse level as one, so check if this mini-quest does not have a min combat level
        if (!questInfo.isMiniQuest()
                || questInfo.getAdditionalRequirements().level().a() != 0) {
            tooltipLines.add((Models.CombatXp.getCombatLevel().current() >= questInfo.getLevel()
                            ? Component.literal("✔").withStyle(ChatFormatting.GREEN)
                            : Component.literal("✖").withStyle(ChatFormatting.RED))
                    .append(Component.literal(" Combat Lv. Min: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(questInfo.getLevel()))
                            .withStyle(ChatFormatting.WHITE)));
        }

        for (Pair<Pair<ProfessionType, Integer>, Boolean> professionRequirement :
                questInfo.getAdditionalRequirements().professionLevels()) {
            MutableComponent base = professionRequirement.b()
                    ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                    : Component.literal("✖ ").withStyle(ChatFormatting.RED);

            tooltipLines.add(
                    base.append(Component.literal(professionRequirement.a().a().getDisplayName() + " Lv. Min: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(
                                            professionRequirement.a().b()))
                                    .withStyle(ChatFormatting.WHITE))));
        }

        for (Pair<String, Boolean> questRequirement :
                questInfo.getAdditionalRequirements().quests()) {
            MutableComponent base = questRequirement.b()
                    ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                    : Component.literal("✖ ").withStyle(ChatFormatting.RED);

            tooltipLines.add(base.append(Component.literal("Quest Req: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(questRequirement.a()))
                            .withStyle(ChatFormatting.WHITE))));
        }

        tooltipLines.add(Component.literal(""));

        tooltipLines.add(Component.literal("-")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" Length: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtils.capitalizeFirst(
                                questInfo.getLength().toString().toLowerCase(Locale.ROOT)))
                        .withStyle(ChatFormatting.WHITE)));
        tooltipLines.add(Component.literal("-")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" Difficulty: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(StringUtils.capitalizeFirst(
                                questInfo.getDifficulty().toString().toLowerCase(Locale.ROOT)))
                        .withStyle(ChatFormatting.WHITE)));

        if (questInfo.getStatus() != ActivityStatus.COMPLETED
                && !questInfo.getNextTask().isEmpty()) {
            tooltipLines.add(Component.literal(""));
            StyledText nextTask = questInfo.getNextTask();
            StyledText[] lines = RenderedStringUtils.wrapTextBySize(nextTask, NEXT_TASK_MAX_WIDTH);

            for (StyledText line : lines) {
                // We use component color inheritance to make sure we don't overwrite colored quest description parts.
                tooltipLines.add(
                        Component.empty().withStyle(ChatFormatting.GRAY).append(line.getComponent()));
            }
        }

        tooltipLines.add(Component.literal(""));
        tooltipLines.add(Component.literal("Rewards:").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (String reward : questInfo.getRewards()) {
            tooltipLines.add(Component.literal("- ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(reward).withStyle(ChatFormatting.GRAY)));
        }

        return tooltipLines;
    }
}
