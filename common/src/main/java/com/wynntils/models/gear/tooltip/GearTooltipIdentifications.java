/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.tooltip;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Pair;
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
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            GearTooltipStyle style) {
        List<Component> identifications = new ArrayList<>();

        List<StatType> listOrdering = Models.Stat.getOrderingList(style.identificationOrdering());
        ArrayList<StatType> allStats = new ArrayList<>(gearInfo.getVariableStats());

        if (gearInstance != null) {
            // If the gear instance contains identifications with stat types not present in the
            // GearInfo, add these as well to the list of stats to be displayed. This should not happen,
            // but might if the GearInfo from the API is not up to date with the actual gear.
            gearInstance.identifications().stream()
                    .map(StatActualValue::statType)
                    .filter(stat -> !allStats.contains(stat))
                    .forEach(allStats::add);
        }

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

            MutableComponent line = getStatLine(statType, gearInfo, gearInstance, currentClass, decorator, style);
            if (line == null) continue;

            identifications.add(line);
            delimiterNeeded = true;
        }

        if (!identifications.isEmpty()
                && identifications.get(identifications.size() - 1).getString().isEmpty()) {
            // Remove last line if it is a delimiter line
            identifications.remove(identifications.size() - 1);
        }

        return identifications;
    }

    private static MutableComponent getStatLine(
            StatType statType,
            GearInfo gearInfo,
            GearInstance gearInstance,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            GearTooltipStyle style) {
        if (gearInstance != null) {
            // We have an actual value
            StatActualValue statActualValue = gearInstance.getActualValue(statType);
            if (statActualValue == null) {
                WynntilsMod.warn("Missing value in item " + gearInfo.name() + " for stat: " + statType);
                return null;
            }

            MutableComponent line = buildIdentifiedLine(gearInfo, style, statActualValue, currentClass);

            StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);
            // Normally this should not happen, but if our API data does not match the
            // actual gear, it might, so handle it gracefully
            if (possibleValues == null) {
                WynntilsMod.warn("Missing stat type in item " + gearInfo.name() + " for stat: " + statType
                        + " which has value: " + statActualValue.value());
                return line;
            }

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
            GearInfo gearInfo, GearTooltipStyle style, StatActualValue actualValue, ClassType currentClass) {
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

        line.append(Component.literal(" " + Models.Stat.getDisplayName(statType, gearInfo, currentClass))
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
        Pair<Integer, Integer> displayRange =
                StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());

        MutableComponent line =
                Component.literal(StringUtils.toSignedString(displayRange.a())).withStyle(colorCode);
        line.append(Component.literal(" to ").withStyle(colorCodeDark));
        line.append(Component.literal(displayRange.b() + statType.getUnit().getDisplayName())
                .withStyle(colorCode));

        line.append(
                Component.literal(" " + Models.Stat.getDisplayName(statType, gearInfo, Models.Character.getClassType()))
                        .withStyle(ChatFormatting.GRAY));

        return line;
    }
}
