/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

public final class CraftedTooltipIdentifications {
    private static final FontDescription ATTRIBUTE_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/attribute/sprite"));
    private static final FontDescription IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));
    private static final String IDENTIFICATION_METER_PREFIX = "\uE023\uDBFF\uDFF7";
    private static final int IDENTIFICATION_METER_MIN = 0xE001;
    private static final int IDENTIFICATION_METER_MAX = 0xE01F;

    public static List<Component> buildTooltip(
            CraftedItemProperty craftedItem,
            ClassType currentClass,
            TooltipStyle style,
            Map<StatType, Component> originalRollWheelSuffixes) {
        List<Component> identifications = new ArrayList<>();

        List<StatType> listOrdering = Models.Stat.getOrderingList(style.identificationOrdering());
        List<StatType> allStats = new ArrayList<>(craftedItem.getStatTypes());

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

            MutableComponent line = getStatLine(statType, craftedItem, currentClass, style, originalRollWheelSuffixes);
            if (line == null) continue;

            identifications.add(line);
            delimiterNeeded = true;
        }

        if (!identifications.isEmpty() && identifications.getLast().getString().isEmpty()) {
            // Remove last line if it is a delimiter line
            identifications.removeLast();
        }

        return identifications;
    }

    private static MutableComponent getStatLine(
            StatType statType,
            CraftedItemProperty craftedItem,
            ClassType currentClass,
            TooltipStyle style,
            Map<StatType, Component> originalRollWheelSuffixes) {
        StatActualValue statActualValue = craftedItem.getIdentifications().stream()
                .filter(stat -> stat.statType() == statType)
                .findFirst()
                .orElse(null);
        if (statActualValue == null) {
            WynntilsMod.warn("Missing value in item " + craftedItem.getName() + " for stat: " + statType);
            return null;
        }

        return buildGearStyledLine(craftedItem, style, statActualValue, currentClass, originalRollWheelSuffixes);
    }

    private static MutableComponent buildGearStyledLine(
            CraftedItemProperty craftedItem,
            TooltipStyle style,
            StatActualValue actualValue,
            ClassType currentClass,
            Map<StatType, Component> originalRollWheelSuffixes) {
        StatType statType = actualValue.statType();
        int value = actualValue.value();
        StatPossibleValues possibleValues = findPossibleValues(craftedItem, statType);

        int valueToShow = statType.calculateAsInverted() ? -value : value;
        boolean hasPositiveEffect = valueToShow > 0 ^ statType.displayAsInverted();
        String displayName =
                Models.Stat.getDisplayName(statType, craftedItem.getRequiredClass(), currentClass, RangedValue.NONE);

        MutableComponent left = Component.empty();
        appendIconPrefix(left, statType);
        left.append(Component.literal(displayName + " ")
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.WHITE)));

        MutableComponent right = Component.literal(StringUtils.toSignedString(valueToShow)
                        + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(hasPositiveEffect ? ChatFormatting.GREEN : ChatFormatting.RED));

        MutableComponent rollSuffix =
                buildCraftedRollSuffix(actualValue, possibleValues, style, originalRollWheelSuffixes.get(statType));
        if (rollSuffix != null) {
            right.append(rollSuffix);
        }

        MutableComponent line = Component.empty().append(left).append(right);
        line.setStyle(line.getStyle().withInsertion(TooltipMarkers.ALIGN_RIGHT.token()));
        return line;
    }

    private static StatPossibleValues findPossibleValues(CraftedItemProperty craftedItem, StatType statType) {
        return craftedItem.getPossibleValues().stream()
                .filter(possibleValues -> possibleValues.statType() == statType)
                .findFirst()
                .orElse(null);
    }

    private static MutableComponent buildCraftedRollSuffix(
            StatActualValue actualValue,
            StatPossibleValues possibleValues,
            TooltipStyle style,
            Component originalRollWheelSuffix) {
        if (possibleValues == null || possibleValues.range().isFixed()) {
            if (!style.showRollWheel() || originalRollWheelSuffix == null) {
                return null;
            }

            return Component.empty().append(originalRollWheelSuffix.copy());
        }

        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        float percentage = StatCalculator.getPercentage(actualValue, possibleValues);

        MutableComponent suffix = ColorScaleUtils.getPercentageTextComponent(
                        feature.getColorMap(), percentage, feature.colorLerp.get(), feature.decimalPlaces.get())
                .withStyle(
                        styleOverride -> styleOverride.withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT));

        if (!style.showRollWheel()) {
            return suffix;
        }

        if (originalRollWheelSuffix != null) {
            suffix.append(originalRollWheelSuffix.copy());
            return suffix;
        }

        suffix.append(buildRollWheel(percentage, suffix.getStyle().getColor()));
        return suffix;
    }

    private static MutableComponent buildRollWheel(float percentage, TextColor color) {
        int glyphOffset = Math.round((percentage / 100f) * (IDENTIFICATION_METER_MAX - IDENTIFICATION_METER_MIN));
        int glyph = Math.max(
                IDENTIFICATION_METER_MIN, Math.min(IDENTIFICATION_METER_MIN + glyphOffset, IDENTIFICATION_METER_MAX));

        MutableComponent wheel = Component.literal(" " + IDENTIFICATION_METER_PREFIX)
                .withStyle(Style.EMPTY
                        .withFont(IDENTIFICATION_METER_FONT)
                        .withColor(ChatFormatting.DARK_GRAY)
                        .withShadowColor(0xFFFFFF));
        wheel.append(Component.literal(Character.toString((char) glyph))
                .withStyle(Style.EMPTY
                        .withFont(IDENTIFICATION_METER_FONT)
                        .withColor(color)
                        .withShadowColor(0xFFFFFF)));
        return wheel;
    }

    private static void appendIconPrefix(MutableComponent line, StatType statType) {
        String iconPrefix = TooltipIdentificationsCompat.getIconPrefix(statType);
        if (iconPrefix.isEmpty()) return;

        line.append(Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(Component.literal(iconPrefix)
                        .withStyle(Style.EMPTY.withFont(ATTRIBUTE_SPRITE_FONT).withColor(ChatFormatting.WHITE))));
    }

    private static final class TooltipIdentificationsCompat {
        private static String getIconPrefix(StatType statType) {
            if (!(statType instanceof SkillStatType skillStatType)) return "";

            return switch (skillStatType.getSkill()) {
                case STRENGTH -> "\uDAFF\uDFFF\uE010\uDB00\uDC02 ";
                case DEXTERITY -> "\uE011\uDB00\uDC02 ";
                case INTELLIGENCE -> "\uDAFF\uDFFF\uE012\uDB00\uDC02 ";
                case DEFENCE -> "\uDAFF\uDFFF\uE013\uDB00\uDC01\uDB00\uDC02 ";
                case AGILITY -> "\uE014\uDB00\uDC02 ";
            };
        }
    }
}
