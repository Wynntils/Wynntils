/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GearRequirementsComponent {
    public List<Component> buildHeaderLines(GearInfo gearInfo, GearInstance gearInstance) {
        List<Component> header = new ArrayList<>();
        header.add(DividerComponent.markLine(Component.empty(), TooltipMarkers.SECTION_DIVIDER));

        GearRequirements gearRequirements = gearInfo.requirements();
        appendSkillRequirements(header, gearRequirements, gearInfo, gearInstance);
        appendQuestRequirement(header, gearRequirements, gearInstance);
        appendClassRequirement(header, gearRequirements);
        appendLevelRequirement(header, gearRequirements);

        if (gearInstance != null && gearInstance.shinyStat().isPresent()) {
            header.add(DividerComponent.markLine(Component.empty(), TooltipMarkers.SECTION_DIVIDER));
            header.add(DividerComponent.markLine(
                    GearTooltipSupport.buildShinyStatLine(
                            gearInstance.shinyStat().get(), gearInfo.tier()),
                    TooltipMarkers.ALIGN_RIGHT));
        }

        header.add(DividerComponent.markLine(Component.empty(), TooltipMarkers.IDENTIFICATION_DIVIDER));
        header.add(DividerComponent.markLine(Component.empty(), TooltipMarkers.REROLL_BANNER));
        return header;
    }

    private static void appendSkillRequirements(
            List<Component> header, GearRequirements gearRequirements, GearInfo gearInfo, GearInstance gearInstance) {
        if (gearRequirements.skills().isEmpty()) {
            return;
        }

        header.add(Component.empty());

        List<MutableComponent> skillIconCells = new ArrayList<>();
        List<MutableComponent> skillCountCells = new ArrayList<>();
        int skillSlotWidth = 0;

        for (Skill skill : Skill.values()) {
            int count = getSkillRequirementCount(gearRequirements, skill);
            MutableComponent iconCell = buildSkillRequirementIconCell(skill, count, gearInfo);
            MutableComponent countCell = buildSkillRequirementCountCell(skill, count, gearInstance);

            skillIconCells.add(iconCell);
            skillCountCells.add(countCell);
            skillSlotWidth = Math.max(skillSlotWidth, McUtils.mc().font.width(iconCell));
            skillSlotWidth = Math.max(skillSlotWidth, McUtils.mc().font.width(countCell));
        }

        header.add(DividerComponent.markLine(
                joinSkillRequirementCells(skillIconCells, skillSlotWidth), TooltipMarkers.ALIGN_CENTER));
        header.add(Component.empty());
        header.add(DividerComponent.markLine(
                joinSkillRequirementCells(skillCountCells, skillSlotWidth), TooltipMarkers.ALIGN_CENTER));
        header.add(Component.empty());
    }

    private static void appendQuestRequirement(
            List<Component> header, GearRequirements gearRequirements, GearInstance gearInstance) {
        if (gearRequirements.quest().isEmpty()) {
            return;
        }

        String questReq = gearRequirements.quest().get();
        Optional<QuestInfo> questInfoOpt = GearTooltipSupport.resolveQuestInfo(questReq);
        int questLevel = questInfoOpt.map(QuestInfo::level).orElse(1);
        boolean fulfilledByQuestState = questInfoOpt
                .map(questInfo -> questInfo.status() == ActivityStatus.COMPLETED)
                .orElse(false);
        boolean fulfilled = fulfilledByQuestState || (gearInstance != null && gearInstance.meetsRequirements());

        MutableComponent questReqLine = Component.literal(" Quest")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.WHITE));
        String shortenedQuestName = StringUtils.shorten(questReq, 10);
        MutableComponent questReqValue = Component.literal(shortenedQuestName + " ")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.GRAY));
        questReqValue.append(Component.literal("(Lv. " + questLevel + ")")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.DARK_GRAY)));

        header.add(DividerComponent.markLine(
                GearTooltipSupport.buildRequirementValueLine(questReqLine, questReqValue, fulfilled),
                TooltipMarkers.ALIGN_RIGHT));
    }

    private static void appendClassRequirement(List<Component> header, GearRequirements gearRequirements) {
        if (gearRequirements.classType().isEmpty()) {
            return;
        }

        ClassType classType = gearRequirements.classType().get();
        boolean fulfilled = Models.Character.getClassType() == classType;

        MutableComponent classReqLine = Component.literal(" Class Type")
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.WHITE));
        MutableComponent classReqValue = Component.literal(classType.getFullName())
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.GRAY));

        header.add(DividerComponent.markLine(
                GearTooltipSupport.buildRequirementValueLine(classReqLine, classReqValue, fulfilled),
                TooltipMarkers.ALIGN_RIGHT));
    }

    private static void appendLevelRequirement(List<Component> header, GearRequirements gearRequirements) {
        int level = gearRequirements.level();
        if (level == 0) {
            return;
        }

        boolean fulfilled = Models.CharacterStats.getLevel() >= level;
        header.add(DividerComponent.markLine(
                GearTooltipSupport.buildRequirementValueLine(
                        Component.literal(" Combat Level")
                                .withStyle(Style.EMPTY
                                        .withFont(
                                                com.wynntils.handlers.tooltip.impl.identifiable
                                                        .IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                                        .withColor(ChatFormatting.WHITE)),
                        Component.literal(String.valueOf(level))
                                .withStyle(Style.EMPTY
                                        .withFont(
                                                com.wynntils.handlers.tooltip.impl.identifiable
                                                        .IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                                        .withColor(ChatFormatting.GRAY)),
                        fulfilled),
                TooltipMarkers.ALIGN_RIGHT));
    }

    private static int getSkillRequirementCount(GearRequirements gearRequirements, Skill skill) {
        return gearRequirements.skills().stream()
                .filter(skillPair -> skillPair.a() == skill)
                .map(Pair::b)
                .findFirst()
                .orElse(0);
    }

    private static MutableComponent buildSkillRequirementIconCell(Skill skill, int count, GearInfo gearInfo) {
        MutableComponent cell = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        MutableComponent iconCluster = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);

        String frame = count == 0
                ? com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SKILL_REQ_FRAME_NONE
                : GearTooltipSupport.getSkillReqFrame(gearInfo.tier());
        iconCluster.append(Component.literal(frame)
                .withStyle(
                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                .SKILL_FRAME_STYLE));
        iconCluster.append(Component.literal("\uDAFF\uDFE7"));
        iconCluster.append(Component.literal(GearTooltipSupport.getSkillReqIcon(skill.ordinal(), count != 0))
                .withStyle(
                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                .SKILL_SPRITE_STYLE));
        cell.append(GearTooltipSupport.withWhiteShadow(iconCluster));
        return cell;
    }

    private static MutableComponent buildSkillRequirementCountCell(Skill skill, int count, GearInstance gearInstance) {
        MutableComponent cell = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);
        String reqCharacter = "\uE005";
        boolean fulfilled = count == 0 || isSkillRequirementFulfilled(skill, count, gearInstance);

        if (count != 0) {
            reqCharacter = fulfilled ? "\uE006" : "\uE007";
        }

        cell.append(GearTooltipSupport.withWhiteShadow(Component.literal(reqCharacter + "\uDAFF\uDFFF")
                .withStyle(
                        com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                .REQUIREMENT_STYLE)));
        cell.append(Component.literal("\uDB00\uDC03")
                .withStyle(com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent.SPACING_STYLE));

        CustomColor color = CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY);
        if (count != 0) {
            color = fulfilled ? CustomColor.fromInt(0xacfac6) : CustomColor.fromInt(0xfaacac);
        }

        cell.append(Component.literal(String.valueOf(count))
                .withStyle(Style.EMPTY
                        .withFont(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(color.asInt())));
        return cell;
    }

    private static MutableComponent joinSkillRequirementCells(List<MutableComponent> cells, int slotWidth) {
        MutableComponent line = Component.empty().withStyle(GearTooltipSupport.WYNNCRAFT_WHITE_STYLE);

        for (int i = 0; i < cells.size(); i++) {
            MutableComponent centeredCell = Component.empty();
            int cellWidth = McUtils.mc().font.width(cells.get(i));
            int remainingWidth = Math.max(0, slotWidth - cellWidth);
            int leftPadding = remainingWidth / 2;
            int rightPadding = remainingWidth - leftPadding;

            GearTooltipSupport.appendOffset(centeredCell, leftPadding);
            centeredCell.append(cells.get(i).copy());
            GearTooltipSupport.appendOffset(centeredCell, rightPadding);

            line.append(centeredCell);
            if (i < cells.size() - 1) {
                line.append(Component.literal("\uDB00\uDC02")
                        .withStyle(
                                com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent
                                        .SPACING_STYLE));
            }
        }

        return line;
    }

    private static boolean isSkillRequirementFulfilled(Skill skill, int count, GearInstance gearInstance) {
        if (gearInstance != null && gearInstance.meetsRequirements()) {
            return true;
        }

        int totalSkillPoints = Models.SkillPoint.getTotalSkillPoints(skill);
        int assignedSkillPoints = Models.SkillPoint.getAssignedSkillPoints(skill);
        boolean hasSkillPointData = Models.SkillPoint.getTotalSum() > 0 || Models.SkillPoint.getAssignedSum() > 0;
        if (!hasSkillPointData) {
            return false;
        }

        return Math.max(totalSkillPoints, assignedSkillPoints) >= count;
    }
}
