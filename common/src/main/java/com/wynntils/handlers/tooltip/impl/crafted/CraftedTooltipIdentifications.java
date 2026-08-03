/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.TooltipLayout;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

final class CraftedTooltipIdentifications {
    private CraftedTooltipIdentifications() {}

    static List<Component> buildTooltip(
            CraftedItemProperty item,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style,
            int targetWidth) {
        return TooltipLayout.align(buildLines(item, currentClass, decorator, style), targetWidth);
    }

    static List<TooltipLine> buildLines(
            CraftedItemProperty item,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style) {
        List<TooltipLine> lines = new ArrayList<>();
        List<StatType> ordering = Models.Stat.getOrderingList(style.ordering());
        List<StatType> allStats = new ArrayList<>(item.getStatTypes());

        item.getIdentifications().stream()
                .map(StatActualValue::statType)
                .filter(stat -> !allStats.contains(stat))
                .forEach(allStats::add);

        boolean delimiterNeeded = false;
        for (StatType statType : ordering) {
            if (style.groupIdentifications() && statType instanceof StatListDelimiter) {
                if (delimiterNeeded) {
                    lines.add(new TooltipLine.Fixed(Component.empty()));
                    delimiterNeeded = false;
                }
            }
            if (!allStats.contains(statType)) continue;

            TooltipLine line = buildLine(item, statType, currentClass, decorator, style);
            if (line == null) continue;

            lines.add(line);
            delimiterNeeded = true;
        }

        if (!lines.isEmpty() && lines.getLast().unaligned().getString().isEmpty()) {
            lines.removeLast();
        }
        return lines;
    }

    private static TooltipLine buildLine(
            CraftedItemProperty item,
            StatType statType,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style) {
        StatActualValue actualValue = item.getIdentifications().stream()
                .filter(stat -> stat.statType() == statType)
                .findFirst()
                .orElse(null);
        if (actualValue == null) {
            WynntilsMod.warn("Missing value in item " + item.getName() + " for stat: " + statType);
            return null;
        }

        StatPossibleValues possibleValues = item.getPossibleValues().stream()
                .filter(possible -> possible.statType() == statType)
                .findFirst()
                .orElse(null);
        MutableComponent suffix = null;
        if (decorator != null
                && (possibleValues == null || !possibleValues.range().isFixed())) {
            suffix = decorator.getSuffix(actualValue, possibleValues, style);
        }

        int value = statType.calculateAsInverted() ? -actualValue.value() : actualValue.value();
        boolean positive = value > 0 ^ statType.displayAsInverted();
        String displayName =
                Models.Stat.getDisplayName(statType, item.getRequiredClass(), currentClass, RangedValue.NONE);

        MutableComponent left = Component.empty();
        appendIconPrefix(left, statType, actualValue.hasIconPrefix());
        left.append(Component.literal(displayName + " ")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.WHITE)));

        MutableComponent right = Component.literal(
                        StringUtils.toSignedString(value) + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor((positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt()));
        if (suffix != null) right.append(suffix);
        return new TooltipLine.Aligned(left, right);
    }

    private static void appendIconPrefix(MutableComponent line, StatType statType, boolean showIconPrefix) {
        if (!showIconPrefix || !(statType instanceof SkillStatType skillStatType)) return;

        String icon =
                switch (skillStatType.getSkill()) {
                    case STRENGTH -> "\uDAFF\uDFFF\uE010\uDB00\uDC02 ";
                    case DEXTERITY -> "\uE011\uDB00\uDC02 ";
                    case INTELLIGENCE -> "\uDAFF\uDFFF\uE012\uDB00\uDC02 ";
                    case DEFENCE -> "\uDAFF\uDFFF\uE013\uDB00\uDC01\uDB00\uDC02 ";
                    case AGILITY -> "\uE014\uDB00\uDC02 ";
                };
        line.append(Component.literal(icon)
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                        .withColor(ChatFormatting.WHITE)
                        .withShadowColor(0xFFFFFF)));
    }
}
