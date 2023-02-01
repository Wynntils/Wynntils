/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class GearTooltipIdentifications {
    public static List<Component> buildTooltip(
            GearInfo gearInfo,
            GearInstance gearInstance,
            TooltipIdentificationDecorator decorator,
            GearTooltipStyle style) {
        List<Component> identifications = new ArrayList<>();

        List<StatType> listOrdering = Models.Stat.getOrderingList(style.identificationOrdering());
        List<StatType> allStats = gearInfo.getVariableStats();
        if (allStats.isEmpty()) return identifications;

        boolean useDelimiters = style.useDelimiters();

        boolean delimiterNeeded = false;
        // We need to iterate over all possible stats in order, to be able
        // to inject delimiters, instead of just using Models.Stat.getSortedStats
        for (StatType statType : listOrdering) {
            if (useDelimiters && statType instanceof StatListDelimiter) {
                if (delimiterNeeded) {
                    identifications.add(Component.literal(""));
                    delimiterNeeded = false;
                }
            }
            // Most stat types are probably not valid for this gear
            if (!allStats.contains(statType)) continue;

            MutableComponent line = getStatLine(statType, gearInfo, gearInstance, decorator, style);
            if (line == null) continue;

            identifications.add(line);
            delimiterNeeded = true;
        }

        if (identifications.get(identifications.size() - 1).getString().isEmpty()) {
            // Remove last line if it is a delimiter line
            identifications.remove(identifications.size() - 1);
        }

        return identifications;
    }

    private static MutableComponent getStatLine(
            StatType statType,
            GearInfo gearInfo,
            GearInstance gearInstance,
            TooltipIdentificationDecorator decorator,
            GearTooltipStyle style) {
        if (gearInstance != null) {
            // We have an actual value
            StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
            StatActualValue statActualValue = gearInstance.getActualValue(statType);
            if (statActualValue == null) {
                WynntilsMod.warn("Missing value in item " + gearInfo.name() + " for stat: " + statType);
                return null;
            }

            MutableComponent line = buildIdentifiedLine(gearInfo, style, statActualValue);

            if (possibleValues.range().isFixed() || decorator == null) return line;

            // Append decorations
            line.append(decorator.getSuffix(statActualValue, possibleValues, style));

            return line;
        } else {
            // Can only show range of possible values
            StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
            return buildUnidentifiedLine(gearInfo, style, possibleValues);
        }
    }

    private static MutableComponent buildIdentifiedLine(
            GearInfo gearInfo, GearTooltipStyle style, StatActualValue actualValue) {
        StatType statType = actualValue.statType();
        int value = actualValue.value();

        int valueToShow = statType.showAsInverted() ? -value : value;
        String starString = style.showStars() ? "***".substring(3 - actualValue.stars()) : "";

        MutableComponent line = Component.literal(StringUtils.toSignedString(valueToShow)
                        + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY.withColor((value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));

        if (!starString.isEmpty()) {
            line.append(Component.literal(starString).withStyle(ChatFormatting.DARK_GREEN));
        }

        line.append(Component.literal(" " + Models.Stat.getDisplayName(statType, gearInfo))
                .withStyle(ChatFormatting.GRAY));

        return line;
    }

    private static MutableComponent buildUnidentifiedLine(
            GearInfo gearInfo, GearTooltipStyle style, StatPossibleValues possibleValues) {
        StatType statType = possibleValues.statType();
        RangedValue valueRange = possibleValues.range();

        // Use value.low as representative; assume both high and low are either < or > 0.
        boolean isGood = valueRange.low() > 0;
        ChatFormatting colorCode = isGood ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting colorCodeDark = isGood ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;

        // Determine which value to show first and which to show last in the "A to B"
        // range displayed
        int first;
        int last;
        if (style.showBestValueLastAlways() || isGood) {
            first = valueRange.low();
            last = valueRange.high();
        } else {
            // Emulate Wynncraft behavior by showing the value closest to zero first
            first = valueRange.high();
            last = valueRange.low();
        }
        // We store "inverted" stats (spell costs) as positive numbers internally,
        // but need to display them as negative numbers
        if (statType.showAsInverted()) {
            first = -first;
            last = -last;
        }

        MutableComponent line =
                Component.literal(StringUtils.toSignedString(first)).withStyle(colorCode);
        line.append(Component.literal(" to ").withStyle(colorCodeDark));
        line.append(
                Component.literal(last + statType.getUnit().getDisplayName()).withStyle(colorCode));

        line.append(Component.literal(" " + Models.Stat.getDisplayName(statType, gearInfo))
                .withStyle(ChatFormatting.GRAY));

        return line;
    }
}
