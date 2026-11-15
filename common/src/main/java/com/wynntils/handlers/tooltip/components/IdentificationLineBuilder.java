/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import static com.wynntils.handlers.tooltip.TooltipBuilder.getNonContributingSkillPrefix;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipOptions.RollStyle;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class IdentificationLineBuilder {
    public static List<TooltipElement> buildIdentificationLines(
            List<Pair<StatType, RangedValue>> ranges, boolean showNonContributingSkillPrefix) {
        List<StatPossibleValues> possibleValues = ranges.stream()
                .map(stat -> {
                    StatType statType = stat.a();
                    RangedValue range = stat.b();
                    // The shared renderer expects spell costs with their internal calculation signs.
                    if (statType.calculateAsInverted()) {
                        range = RangedValue.of(-range.high(), -range.low());
                    }
                    return new StatPossibleValues(statType, range, range.low(), range.isFixed());
                })
                .toList();

        return buildIdentificationLines(
                List.of(),
                possibleValues,
                false,
                null,
                RangedValue.NONE,
                showNonContributingSkillPrefix,
                IdentificationDisplay.PERCENTAGE,
                RollStyle.NONE);
    }

    public static List<TooltipElement> buildIdentificationLines(
            List<StatActualValue> identifications,
            GearRequirements gearRequirements,
            boolean showNonContributingSkillPrefix,
            RollStyle rollStyle) {
        return buildIdentificationLines(
                identifications,
                List.of(),
                true,
                gearRequirements,
                RangedValue.NONE,
                showNonContributingSkillPrefix,
                IdentificationDisplay.PERCENTAGE,
                rollStyle);
    }

    public static List<TooltipElement> buildIdentificationLines(
            List<StatActualValue> identifications,
            List<StatPossibleValues> possibleValuesList,
            boolean identified,
            GearRequirements gearRequirements,
            RangedValue levelRange,
            boolean showNonContributingSkillPrefix,
            IdentificationDisplay display,
            RollStyle rollStyle) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        List<StatType> allStats = new ArrayList<>();

        possibleValuesList.stream().map(StatPossibleValues::statType).forEach(allStats::add);

        identifications.stream()
                .map(StatActualValue::statType)
                .filter(stat -> !allStats.contains(stat))
                .forEach(allStats::add);

        Map<StatType, TooltipElement> statLines = new HashMap<>();
        for (StatType statType : allStats) {
            Component statDisplayName =
                    buildStatDisplayName(statType, gearRequirements, levelRange, showNonContributingSkillPrefix);

            // We have an actual value
            StatActualValue statActualValue = identifications.stream()
                    .filter(stat -> stat.statType() == statType)
                    .findFirst()
                    .orElse(null);
            if (identified && statActualValue == null) continue;

            StatPossibleValues possibleValues = possibleValuesList.stream()
                    .filter(stat -> stat.statType() == statType)
                    .findFirst()
                    .orElse(null);
            // Crafted items have no possible ranges. Also use the vanilla meter when API data is missing.
            if (possibleValues == null) {
                if (statActualValue != null) {
                    statLines.put(
                            statType,
                            TooltipLine.leftRight(
                                    statDisplayName, getEstimatedStatValue(statType, statActualValue, rollStyle)));
                }
                continue;
            }

            Component statValue;
            Pair<Integer, Integer> range =
                    StatCalculator.getDisplayRange(possibleValues, options.showBestValueLastAlways());
            boolean positive = range.a() > 0 ^ statType.displayAsInverted();
            int color = (positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt();
            int darkColor = (positive ? CommonColors.WYNNCRAFT_DARK_GREEN : CommonColors.WYNNCRAFT_DARK_RED).asInt();

            if (statActualValue != null && !possibleValues.range().inRange(statActualValue.value())) {
                statValue = getEstimatedStatValue(statType, statActualValue, rollStyle);
            } else if (possibleValues.isPreIdentified()) {
                int valueToShow =
                        statType.calculateAsInverted() ? -possibleValues.baseValue() : possibleValues.baseValue();
                statValue = Component.literal(StringUtils.toSignedCommaString(valueToShow)
                                + statType.getUnit().getDisplayName())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(color));
            } else if (!identified) {
                statValue = Component.empty()
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))
                        .append(Component.literal(StringUtils.toSignedCommaString(range.a())
                                        + statType.getUnit().getDisplayName())
                                .withColor(color))
                        .append(Component.literal(" to ").withStyle(Style.EMPTY.withColor(darkColor)))
                        .append(Component.literal(StringUtils.toSignedCommaString(range.b())
                                        + statType.getUnit().getDisplayName())
                                .withColor(color));
            } else {
                statValue = getStatValue(
                        statType, statActualValue, range, possibleValues, color, darkColor, display, rollStyle);
            }

            statLines.put(statType, TooltipLine.leftRight(statDisplayName, statValue));
        }

        return buildLines(statLines);
    }

    private static Component getStatValue(
            StatType statType,
            StatActualValue actualValue,
            Pair<Integer, Integer> range,
            StatPossibleValues possibleValues,
            int color,
            int darkColor,
            IdentificationDisplay display,
            RollStyle rollStyle) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        int value = actualValue.value();
        int valueToShow = statType.calculateAsInverted() ? -value : value;
        MutableComponent statValue = Component.empty()
                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))
                .append(Component.literal(StringUtils.toSignedCommaString(valueToShow)
                                + statType.getUnit().getDisplayName())
                        .withColor(color));

        if (display == IdentificationDisplay.PERCENTAGE) {
            MutableComponent percentage = Component.empty();

            if (rollStyle == RollStyle.PERCENTAGE) {
                percentage = ColorScaleUtils.getPercentageTextComponent(
                                options.colorMap(),
                                StatCalculator.calculatePercentage(actualValue, possibleValues)
                                        .value(),
                                options.colorLerp(),
                                options.rainbowInternalRoll() && actualValue.perfectInternalRoll(),
                                options.decimalPlaces(),
                                StatCalculator.calculatePercentage(actualValue, possibleValues)
                                        .estimated())
                        .withStyle(style -> style.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT));
            } else if (rollStyle == RollStyle.WHEEL) {
                percentage = Component.literal(" ")
                        .append(ColorScaleUtils.getWheelTextComponent(
                                options.colorMap(),
                                StatCalculator.calculatePercentage(actualValue, possibleValues)
                                        .value(),
                                options.colorLerp(),
                                options.rainbowInternalRoll() && actualValue.perfectInternalRoll()));
            }

            statValue.append(percentage);
        } else if (display == IdentificationDisplay.STAT_RANGES) {
            statValue
                    .append(Component.literal(" "))
                    .append(Component.literal("[").withColor(darkColor))
                    .append(Component.literal(String.valueOf(range.a())).withColor(color))
                    .append(Component.literal(", ").withColor(darkColor))
                    .append(Component.literal(String.valueOf(range.b())).withColor(color))
                    .append(Component.literal("]"));
        } else if (display == IdentificationDisplay.REROLL_CHANCE) {
            statValue
                    .append(Component.literal(" "))
                    .append(Component.literal("✮")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)))
                    .append(Component.literal(String.format(
                                    Locale.ROOT, "%.2f%%", StatCalculator.getPerfectChance(possibleValues)))
                            .withColor(CommonColors.WYNNCRAFT_AQUA.asInt()))
                    .append(Component.literal(" "))
                    .append(Component.literal("\u2191")
                            .withStyle(Style.EMPTY
                                    .withFont(FontDescription.DEFAULT)
                                    .withColor(CommonColors.WYNNCRAFT_GREEN.asInt())))
                    .append(Component.literal(String.format(
                                    Locale.ROOT,
                                    "%.1f%%",
                                    StatCalculator.getIncreaseChance(actualValue, possibleValues)))
                            .withColor(CommonColors.WYNNCRAFT_GREEN.asInt()))
                    .append(Component.literal(" "))
                    .append(Component.literal("\u2193")
                            .withStyle(Style.EMPTY
                                    .withFont(FontDescription.DEFAULT)
                                    .withColor(CommonColors.WYNNCRAFT_RED.asInt())))
                    .append(Component.literal(String.format(
                                    Locale.ROOT,
                                    "%.1f%%",
                                    StatCalculator.getDecreaseChance(actualValue, possibleValues)))
                            .withColor(CommonColors.WYNNCRAFT_RED.asInt()));
        } else {
            statValue
                    .append(Component.literal(" "))
                    .append(Component.literal("<").withColor(darkColor))
                    .append(Component.literal(actualValue.internalRoll().low() + "%")
                            .withColor(color))
                    .append(Component.literal(" to ").withColor(darkColor))
                    .append(Component.literal(actualValue.internalRoll().high() + "%")
                            .withColor(color))
                    .append(Component.literal(">").withColor(darkColor));
        }

        return statValue;
    }

    private static Component getEstimatedStatValue(
            StatType statType, StatActualValue actualValue, RollStyle rollStyle) {
        int value = statType.calculateAsInverted() ? -actualValue.value() : actualValue.value();
        boolean positive = value > 0 ^ statType.displayAsInverted();
        int color = (positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED).asInt();

        return getMeterStatValue(statType, actualValue, color, rollStyle);
    }

    private static Component getMeterStatValue(
            StatType statType, StatActualValue actualValue, int color, RollStyle rollStyle) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        int value = actualValue.value();
        int valueToShow = statType.calculateAsInverted() ? -value : value;
        MutableComponent statValue = Component.empty()
                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))
                .append(Component.literal(StringUtils.toSignedCommaString(valueToShow)
                                + statType.getUnit().getDisplayName())
                        .withColor(color));

        MutableComponent percentage = Component.empty();

        if (actualValue.vanillaMeter() == CappedValue.EMPTY) return statValue;

        if (rollStyle == RollStyle.PERCENTAGE) {
            percentage = ColorScaleUtils.getPercentageTextComponent(
                            options.colorMap(),
                            (float) actualValue.vanillaMeter().getPercentage(),
                            options.colorLerp(),
                            options.rainbowInternalRoll() && actualValue.perfectInternalRoll(),
                            options.decimalPlaces(),
                            true)
                    .withStyle(style -> style.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT));
        } else if (rollStyle == RollStyle.WHEEL) {
            percentage = Component.literal(" ")
                    .append(ColorScaleUtils.getWheelTextComponent(
                            options.colorMap(),
                            actualValue.vanillaMeter(),
                            options.colorLerp(),
                            options.rainbowInternalRoll() && actualValue.perfectInternalRoll()));
        }

        statValue.append(percentage);

        return statValue;
    }

    public static Component buildStatDisplayName(
            StatType statType, GearRequirements requirements, RangedValue levelRange, boolean showSkillPrefix) {
        MutableComponent name = Component.empty();

        if (showSkillPrefix && statType instanceof SkillStatType skillStat) {
            name.append(getNonContributingSkillPrefix(skillStat.getSkill()));
        }

        return name.append(Component.literal(Models.Stat.getDisplayName(
                        statType,
                        requirements == null ? null : requirements.classType().orElse(null),
                        Models.Character.getClassType(),
                        levelRange))
                .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT));
    }

    static List<TooltipElement> buildLines(Map<StatType, TooltipElement> statLines) {
        TooltipOptions options = Handlers.Tooltip.getTooltipOptions();

        List<TooltipElement> lines = new ArrayList<>();
        boolean delimiterNeeded = false;
        // Iterate over the full ordering so group delimiters appear between the relevant stats.
        for (StatType statType : Models.Stat.getOrderingList(options.identificationOrdering())) {
            if (options.groupIdentifications() && statType instanceof StatListDelimiter && delimiterNeeded) {
                lines.add(TooltipLine.empty());
                delimiterNeeded = false;
            }

            TooltipElement line = statLines.get(statType);
            if (line == null) continue;

            lines.add(line);
            delimiterNeeded = true;
        }

        if (!lines.isEmpty() && lines.getLast().equals(TooltipLine.empty())) {
            // Remove last line if it is a delimiter line
            lines.removeLast();
        }

        return lines;
    }
}
