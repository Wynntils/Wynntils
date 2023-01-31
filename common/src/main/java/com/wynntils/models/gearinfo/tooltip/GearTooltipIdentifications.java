/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
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

            MutableComponent line;
            if (gearInstance != null) {
                // Put in actual value
                StatActualValue statActualValue = gearInstance.getActualValue(statType);
                if (statActualValue == null) {
                    // FIXME: spell cost cause this to explode...
                    //      WynntilsMod.warn("Missing value in item " + gearInfo.name() + " for stat: " + statType);
                    continue;
                }

                line = buildIdentifiedLine(gearInfo, style, statActualValue);
                StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
                if (!possibleValues.range().isFixed() && decorator != null) {
                    MutableComponent suffix = decorator.getSuffix(statActualValue, possibleValues, style);
                    line.append(suffix);
                }

            } else {
                // Put in range of possible values
                StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
                line = buildUnidentifiedLine(gearInfo, style, possibleValues);
            }
            identifications.add(line);
            delimiterNeeded = true;
        }

        if (identifications.get(identifications.size() - 1).getString().isEmpty()) {
            // Remove last line if it is a delimiter line
            identifications.remove(identifications.size() - 1);
        }

        return identifications;
    }

    private static MutableComponent buildUnidentifiedLine(
            GearInfo gearInfo, GearTooltipStyle style, StatPossibleValues possibleValues) {
        String inGameName = possibleValues.stat().getDisplayName();
        StatUnit unitType = possibleValues.stat().getUnit();
        boolean invert = possibleValues.stat().showAsInverted();

        RangedValue value = possibleValues.range();
        String unit = unitType.getDisplayName();
        // Use value.low as representative; assume both high and low are either < or > 0.
        boolean isGood = value.low() > 0;
        ChatFormatting colorCode = isGood ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting colorCodeDark = isGood ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;

        MutableComponent baseComponent = Component.literal("");

        int first;
        int last;
        if (style.showBestValueLastAlways() || isGood) {
            first = value.low();
            last = value.high();
        } else {
            // Emulate Wynncraft behavior by showing the value closest to zero first
            first = value.high();
            last = value.low();
        }
        // We store "inverted" stats (spell costs) as positive numbers internally,
        // but need to display them as negative numbers
        if (possibleValues.stat().showAsInverted()) {
            first = -first;
            last = -last;
        }
        baseComponent.append(
                Component.literal(StringUtils.toSignedString(first)).withStyle(colorCode));
        baseComponent.append(Component.literal(" to ").withStyle(colorCodeDark));
        baseComponent.append(Component.literal(last + unit).withStyle(colorCode));

        baseComponent.append(Component.literal(" " + inGameName).withStyle(ChatFormatting.GRAY));

        return baseComponent;
    }

    private static MutableComponent buildIdentifiedLine(
            GearInfo gearInfo, GearTooltipStyle style, StatActualValue actualValue) {
        StatType statType = actualValue.stat();
        String starString = style.showStars() ? "***".substring(3 - actualValue.stars()) : "";

        String inGameName = statType.getDisplayName();
        int value = actualValue.value();
        StatUnit unitType = statType.getUnit();
        boolean invert = statType.showAsInverted();
        String unit = unitType.getDisplayName();

        MutableComponent baseComponent = Component.literal("");

        int valueToShow = invert ? -value : value;

        MutableComponent statInfo = Component.literal(StringUtils.toSignedString(valueToShow) + unit);
        boolean isGood = (value > 0);
        statInfo.setStyle(Style.EMPTY.withColor(isGood ? ChatFormatting.GREEN : ChatFormatting.RED));

        baseComponent.append(statInfo);

        if (!starString.isEmpty()) {
            baseComponent.append(Component.literal(starString).withStyle(ChatFormatting.DARK_GREEN));
        }

        baseComponent.append(Component.literal(" " + inGameName).withStyle(ChatFormatting.GRAY));

        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
        if (possibleValues.range().isFixed()) return baseComponent;

        return baseComponent;
    }
}
