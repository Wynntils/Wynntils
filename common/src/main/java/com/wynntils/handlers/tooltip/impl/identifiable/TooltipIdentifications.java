/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.TooltipLayout;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class TooltipIdentifications {
    public static List<Component> buildTooltip(
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style,
            int targetWidth) {
        return TooltipLayout.align(buildLines(itemInfo, currentClass, decorator, style), targetWidth);
    }

    public static List<TooltipLine> buildLines(
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style) {
        List<TooltipLine> lines = new ArrayList<>();
        List<StatType> ordering = Models.Stat.getOrderingList(style.ordering());
        List<StatType> allStats = new ArrayList<>(itemInfo.getVariableStats());

        itemInfo.getIdentifications().stream()
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

            Pair<MutableComponent, MutableComponent> line =
                    getStatLineParts(statType, itemInfo, currentClass, decorator, style);
            if (line == null) continue;

            lines.add(new TooltipLine.Aligned(line.a(), line.b()));
            delimiterNeeded = true;
        }

        if (!lines.isEmpty() && lines.getLast().unaligned().getString().isEmpty()) {
            lines.removeLast();
        }
        return lines;
    }

    private static Pair<MutableComponent, MutableComponent> getStatLineParts(
            StatType statType,
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style) {
        if (itemInfo.getIdentifications().isEmpty()) {
            StatPossibleValues possibleValues = findPossibleValues(itemInfo, statType);
            if (possibleValues == null) {
                WynntilsMod.warn("Missing possible values in item " + itemInfo.getName() + " for stat: " + statType);
                return null;
            }
            return buildUnidentifiedLineParts(itemInfo, style, possibleValues);
        }

        StatActualValue actualValue = itemInfo.getIdentifications().stream()
                .filter(stat -> stat.statType() == statType)
                .findFirst()
                .orElse(null);
        if (actualValue == null) {
            WynntilsMod.warn("Missing value in item " + itemInfo.getName() + " for stat: " + statType);
            return null;
        }

        MutableComponent suffix = null;
        StatPossibleValues possibleValues = findPossibleValues(itemInfo, statType);
        if (possibleValues == null) {
            WynntilsMod.warn("Missing stat type in item " + itemInfo.getName() + " for stat: " + statType
                    + " which has value: " + actualValue.value());
        } else if (!possibleValues.range().isFixed() && decorator != null) {
            suffix = decorator.getSuffix(actualValue, possibleValues, style);
        }
        return buildIdentifiedLineParts(itemInfo, actualValue, currentClass, suffix);
    }

    private static StatPossibleValues findPossibleValues(IdentifiableItemProperty<?, ?> itemInfo, StatType statType) {
        return itemInfo.getPossibleValues().stream()
                .filter(stat -> stat.statType() == statType)
                .findFirst()
                .orElse(null);
    }

    private static Pair<MutableComponent, MutableComponent> buildIdentifiedLineParts(
            IdentifiableItemProperty<?, ?> itemInfo,
            StatActualValue actualValue,
            ClassType currentClass,
            MutableComponent suffix) {
        StatType statType = actualValue.statType();
        int value = statType.calculateAsInverted() ? -actualValue.value() : actualValue.value();
        boolean positive = value > 0 ^ statType.displayAsInverted();
        String displayName = Models.Stat.getDisplayName(
                statType, itemInfo.getRequiredClass(), currentClass, itemInfo.getIdentificationLevelRange());

        MutableComponent left = Component.empty();
        appendIconPrefix(left, statType, actualValue.hasIconPrefix());
        left.append(Component.literal(displayName + " ")
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.WHITE)));

        MutableComponent right = Component.literal(
                        StringUtils.toSignedString(value) + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_FONT)
                        .withColor((positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt()));
        if (suffix != null) right.append(suffix);
        return Pair.of(left, right);
    }

    private static Pair<MutableComponent, MutableComponent> buildUnidentifiedLineParts(
            IdentifiableItemProperty<?, ?> itemInfo, TooltipStyle style, StatPossibleValues possibleValues) {
        StatType statType = possibleValues.statType();
        Pair<Integer, Integer> range = StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());
        boolean positive = range.a() > 0 ^ statType.displayAsInverted();
        int color = (positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt();
        int darkColor = (positive ? CommonColors.WYNNCRAFT_DARK_GREEN : CommonColors.WYNNCRAFT_DARK_RED).asInt();
        String displayName = Models.Stat.getDisplayName(
                statType,
                itemInfo.getRequiredClass(),
                Models.Character.getClassType(),
                itemInfo.getIdentificationLevelRange());

        MutableComponent left = Component.literal(displayName + " ")
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(ChatFormatting.WHITE));
        MutableComponent right = Component.literal(StringUtils.toSignedString(range.a()))
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(color));
        right.append(Component.literal(" to ")
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(darkColor)));
        right.append(Component.literal(range.b() + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FONT).withColor(color)));
        return Pair.of(left, right);
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
                        .withFont(CommonFonts.ATTRIBUTE_SPRITE_FONT)
                        .withColor(ChatFormatting.WHITE)
                        .withShadowColor(0xFFFFFF)));
    }
}
