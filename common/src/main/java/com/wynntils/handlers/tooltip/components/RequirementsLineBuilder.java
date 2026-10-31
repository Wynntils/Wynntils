/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.FontManager;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipGrid;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.activities.quests.SimpleQuestInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.OptionalBoolean;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class RequirementsLineBuilder {
    private static final int SKILL_REQ_TABLE_WIDTH = 150;

    public static List<TooltipElement> buildRequirements(
            GearTier tier, GearRequirements gearRequirements, GearInstanceRequirements gearInstanceRequirements) {
        List<TooltipElement> lines = new ArrayList<>();

        if (gearRequirements != null && !gearRequirements.skills().isEmpty()) {
            lines.addAll(buildSkillReqLines(gearRequirements.skills(), gearInstanceRequirements, tier));
        }

        if (gearRequirements != null && gearRequirements.quest().isPresent()) {
            TooltipElement questLine = buildQuestRequirementLine(
                    gearRequirements.quest().get(),
                    true,
                    gearInstanceRequirements == null
                            ? OptionalBoolean.NULL
                            : (gearInstanceRequirements.questReqMet() ? OptionalBoolean.TRUE : OptionalBoolean.FALSE),
                    gearRequirements.questLevel());

            if (questLine != null) {
                lines.add(questLine);
            }
        }

        if (gearRequirements != null && gearRequirements.classType().isPresent()) {
            TooltipElement classLine = gearInstanceRequirements == null
                    ? buildClassRequirementLine(gearRequirements.classType().get())
                    : buildClassRequirementLine(
                            gearRequirements.classType().get(),
                            gearInstanceRequirements.classReqMet() ? OptionalBoolean.TRUE : OptionalBoolean.FALSE);

            lines.add(classLine);
        }

        int levelRequirement = gearRequirements == null ? 0 : gearRequirements.level();
        if (levelRequirement > 0) {
            TooltipElement levelLine = gearInstanceRequirements == null
                    ? buildLevelRequirementLine(levelRequirement)
                    : buildLevelRequirementLine(
                            levelRequirement,
                            gearInstanceRequirements.levelReqMet() ? OptionalBoolean.TRUE : OptionalBoolean.FALSE);

            lines.add(levelLine);
        }

        return lines;
    }

    public static TooltipElement buildQuestRequirementLine(
            String questRequirement, boolean includeLevel, OptionalBoolean requirementMet) {
        return buildQuestRequirementLine(questRequirement, includeLevel, requirementMet, 0);
    }

    private static TooltipElement buildQuestRequirementLine(
            String questRequirement, boolean includeLevel, OptionalBoolean requirementMet, int parsedLevel) {
        SimpleQuestInfo questInfo = Models.Quest.getQuestInfo(questRequirement);

        // The API still contains quest requirements from removed quests so if we've marked it as a removed quest then
        // don't add the requirement line
        if (parsedLevel == 0 && questInfo.removed()) return null;

        String displayName = questInfo == SimpleQuestInfo.UNKNOWN ? questRequirement : questInfo.displayName();
        int level = parsedLevel > 0 ? parsedLevel : questInfo.level();

        MutableComponent questName = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(Component.literal(StringUtils.shorten(displayName, 10)));

        if (includeLevel) {
            questName
                    .append(Component.literal(" "))
                    .append(Component.literal("(Lv. " + level + ")").withStyle(ChatFormatting.DARK_GRAY));
        }

        return buildRequirementLine("Quest", requirementMet, questName);
    }

    public static TooltipElement buildClassRequirementLine(ClassType classRequirement) {
        ClassType currentClass = Models.Character.getClassType();

        OptionalBoolean metRequirement;
        if (classRequirement == currentClass) {
            metRequirement = OptionalBoolean.TRUE;
        } else if (currentClass == ClassType.NONE) {
            metRequirement = OptionalBoolean.NULL;
        } else {
            metRequirement = OptionalBoolean.FALSE;
        }

        return buildClassRequirementLine(classRequirement, metRequirement);
    }

    public static TooltipElement buildClassRequirementLine(ClassType classRequirement, OptionalBoolean requirementMet) {
        return buildRequirementLine(
                "Class Type",
                requirementMet,
                Component.literal(classRequirement.getFullName()).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
    }

    public static TooltipElement buildLevelRequirementLine(int levelRequirement) {
        return buildLevelRequirementLine(
                levelRequirement,
                Models.CombatXp.getCombatLevel().current() >= levelRequirement
                        ? OptionalBoolean.TRUE
                        : OptionalBoolean.FALSE);
    }

    public static TooltipElement buildLevelRequirementLine(int levelRequirement, OptionalBoolean requirementMet) {
        return buildRequirementLine(
                "Combat Level",
                requirementMet,
                Component.literal(String.valueOf(levelRequirement)).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
    }

    public static TooltipElement buildProfessionLevelRequirementLine(
            ProfessionType professionType, int levelRequirement, boolean includeIcon) {
        MutableComponent professionName = Component.empty().withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT);

        if (includeIcon) {
            professionName.append(Component.literal(getProfessionIcon(professionType))
                    .withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT)));
        }

        professionName.append(Component.literal(" " + professionType.getDisplayName() + " Level"));

        return buildRequirementLine(
                professionName,
                Models.Profession.getLevel(professionType) >= levelRequirement
                        ? OptionalBoolean.TRUE
                        : OptionalBoolean.FALSE,
                Component.literal(String.valueOf(levelRequirement)).withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT));
    }

    public static TooltipElement buildRequirementLine(
            String requirementName, OptionalBoolean requirementMet, Component requirementValue) {
        return buildRequirementLine(
                Component.literal(requirementName).withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT),
                requirementMet,
                requirementValue);
    }

    public static TooltipElement buildRequirementLine(
            Component requirementName, OptionalBoolean requirementMet, Component requirementValue) {
        Component requirementCondition = Component.empty()
                .append(Component.literal(checkbox(requirementMet) + "\uDAFF\uDFFF ")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)
                                .withoutShadow()))
                .append(requirementName);

        return TooltipLine.leftRight(requirementCondition, requirementValue);
    }

    private static List<TooltipElement> buildSkillReqLines(
            List<Pair<Skill, Integer>> skillReqs, GearInstanceRequirements gearInstanceRequirements, GearTier tier) {
        List<TooltipElement> lines = new ArrayList<>();

        MutableComponent frames = Component.empty();
        MutableComponent values = Component.empty();

        for (Skill skill : Skill.values()) {
            int count = skillReqs.stream()
                    .filter(req -> req.a() == skill)
                    .findFirst()
                    .map(Pair::b)
                    .orElse(0);
            boolean fulfilled = gearInstanceRequirements != null
                    && gearInstanceRequirements.skillReqsMet().getOrDefault(skill, false);

            appendSkillCell(frames, buildSkillFrame(skill, count, tier), 29);
            appendSkillCell(values, buildSkillValue(count, fulfilled), 30);
        }

        lines.add(TooltipLine.empty());
        lines.add(skillReqGrid(frames, 0));
        lines.add(TooltipLine.empty());
        lines.add(skillReqGrid(values, SKILL_REQ_TABLE_WIDTH));
        lines.add(TooltipLine.empty());

        return lines;
    }

    private static void appendSkillCell(MutableComponent row, Component content, int width) {
        int padding = Math.max(0, (width - McUtils.mc().font.width(content)) / 2);
        Component space =
                Component.literal(FontManager.calculateOffset(0, padding)).withStyle(CommonStyles.SPACE);
        row.append(space).append(content).append(space);
    }

    private static TooltipGrid skillReqGrid(Component row, int rowWidth) {
        return TooltipGrid.builder(1)
                .cellWidth(rowWidth)
                .tableWidth(SKILL_REQ_TABLE_WIDTH)
                .alignment(TooltipAlignment.CENTER)
                .add(row, TooltipAlignment.LEFT)
                .build();
    }

    private static Component buildSkillFrame(Skill skill, int count, GearTier tier) {
        String frame = count == 0 ? "\uE007" : skillFrame(tier);
        String sprite = String.valueOf((char) ((count == 0 ? '\uE010' : '\uE000') + skill.ordinal()));

        return Component.empty()
                .withStyle(Style::withoutShadow)
                .append(Component.literal(frame)
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_FRAME_FONT)))
                .append(Component.literal("\uDAFF\uDFE7").withStyle(CommonStyles.SPACE))
                .append(Component.literal(sprite)
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)));
    }

    private static Component buildSkillValue(int count, boolean fulfilled) {
        String icon = count == 0 ? "\uE005" : fulfilled ? "\uE006" : "\uE007";

        return Component.empty()
                .append(Component.literal(icon + "\uDAFF\uDFFF")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)))
                .append(Component.literal("\uDB00\uDC03").withStyle(CommonStyles.SPACE))
                .append(Component.literal(String.valueOf(count))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(
                                        count == 0
                                                ? CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY)
                                                        .asInt()
                                                : (fulfilled
                                                        ? CommonColors.WYNNCRAFT_GREEN.asInt()
                                                        : CommonColors.WYNNCRAFT_RED.asInt()))));
    }

    private static String skillFrame(GearTier tier) {
        GearTier[] tiers = GearTier.validValues();
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i] == tier) {
                return String.valueOf((char) ('\uE000' + i));
            }
        }

        return "\uE007";
    }

    private static String checkbox(OptionalBoolean fulfilled) {
        return switch (fulfilled) {
            case NULL -> "\uE005";
            case TRUE -> "\uE006";
            case FALSE -> "\uE007";
        };
    }

    private static String getProfessionIcon(ProfessionType professionType) {
        return switch (professionType) {
            case FARMING -> "\uE000";
            case FISHING -> "\uE001\uDAFF\uDFFE";
            case MINING -> "\uE002\uDAFF\uDFFE";
            case WOODCUTTING -> "\uDB00\uDC01\uE003\uDAFF\uDFFF";
            default -> "";
        };
    }
}
