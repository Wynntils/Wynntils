/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListDelimiter;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class TooltipIdentifications {
    private static final FontDescription ATTRIBUTE_SPRITE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/attribute/sprite"));

    public static List<Component> buildTooltip(
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style,
            int targetWidth) {
        List<Object> lineEntries = new ArrayList<>();
        List<Pair<MutableComponent, MutableComponent>> statLines = new ArrayList<>();

        List<StatType> listOrdering = Models.Stat.getOrderingList(style.identificationOrdering());
        List<StatType> allStats = new ArrayList<>(itemInfo.getVariableStats());

        // If the item instance contains identifications with stat types not present in the
        // variable stats list, add these as well to the list of stats to be displayed.
        // This should not happen, but might if the info from the API is not up to date with the actual item.
        itemInfo.getIdentifications().stream()
                .map(StatActualValue::statType)
                .filter(stat -> !allStats.contains(stat))
                .forEach(allStats::add);

        if (allStats.isEmpty()) return List.of();

        boolean useDelimiters = style.useDelimiters();
        boolean markForPostAlignment = shouldMarkIdentificationAlignment(itemInfo);

        boolean delimiterNeeded = false;
        // We need to iterate over all possible stats in order, to be able
        // to inject delimiters, instead of just using Models.Stat.getSortedStats
        for (StatType statType : listOrdering) {
            if (useDelimiters && statType instanceof StatListDelimiter) {
                if (delimiterNeeded) {
                    lineEntries.add(Component.literal(""));
                    delimiterNeeded = false;
                }
            }
            // Most stat types are probably not valid for this gear
            if (!allStats.contains(statType)) continue;

            Pair<MutableComponent, MutableComponent> line =
                    getStatLineParts(statType, itemInfo, currentClass, decorator, style);
            if (line == null) continue;

            lineEntries.add(line);
            statLines.add(line);
            delimiterNeeded = true;
        }

        if (lineEntries.isEmpty()) return List.of();

        int maxCombinedWidth = targetWidth;
        for (Pair<MutableComponent, MutableComponent> statLine : statLines) {
            int width =
                    McUtils.mc().font.width(statLine.a()) + McUtils.mc().font.width(statLine.b());
            if (width > maxCombinedWidth) {
                maxCombinedWidth = width;
            }
        }

        List<Component> identifications = new ArrayList<>();
        for (Object entry : lineEntries) {
            if (entry instanceof Component component) {
                identifications.add(component);
                continue;
            }

            @SuppressWarnings("unchecked")
            Pair<MutableComponent, MutableComponent> lineParts = (Pair<MutableComponent, MutableComponent>) entry;
            MutableComponent left = lineParts.a();
            MutableComponent right = lineParts.b();

            MutableComponent line = Component.empty().append(left);
            int currentWidth = McUtils.mc().font.width(left) + McUtils.mc().font.width(right);
            if (currentWidth < maxCombinedWidth) {
                String offset = Managers.Font.calculateOffset(currentWidth, maxCombinedWidth);
                if (!offset.isEmpty()) {
                    line.append(Component.literal(offset).withStyle(IdentifiableTooltipComponent.SPACING_STYLE));
                }
            }
            line.append(right);
            if (markForPostAlignment) {
                line.setStyle(line.getStyle().withInsertion(TooltipMarkers.ALIGN_RIGHT.token()));
            }
            identifications.add(line);
        }

        if (!identifications.isEmpty() && identifications.getLast().getString().isEmpty()) {
            // Remove last line if it is a delimiter line
            identifications.removeLast();
        }

        return identifications;
    }

    private static Pair<MutableComponent, MutableComponent> getStatLineParts(
            StatType statType,
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style) {
        if (!itemInfo.getIdentifications().isEmpty()) {
            // We have an actual value
            StatActualValue statActualValue = itemInfo.getIdentifications().stream()
                    .filter(stat -> stat.statType() == statType)
                    .findFirst()
                    .orElse(null);
            if (statActualValue == null) {
                WynntilsMod.warn("Missing value in item " + itemInfo.getName() + " for stat: " + statType);
                return null;
            }

            MutableComponent suffix = null;

            StatPossibleValues possibleValues = itemInfo.getPossibleValues().stream()
                    .filter(stat -> stat.statType() == statType)
                    .findFirst()
                    .orElse(null);
            // Normally this should not happen, but if our API data does not match the
            // actual gear, it might, so handle it gracefully
            if (possibleValues == null) {
                WynntilsMod.warn("Missing stat type in item " + itemInfo.getName() + " for stat: " + statType
                        + " which has value: " + statActualValue.value());
                return buildIdentifiedLineParts(itemInfo, style, statActualValue, currentClass, null);
            }

            if (!possibleValues.range().isFixed() && decorator != null) {
                suffix = withWynncraftFont(decorator.getSuffix(statActualValue, possibleValues, style));
            }

            return buildIdentifiedLineParts(itemInfo, style, statActualValue, currentClass, suffix);
        } else {
            // Can only show range of possible values
            StatPossibleValues possibleValues = itemInfo.getPossibleValues().stream()
                    .filter(stat -> stat.statType() == statType)
                    .findFirst()
                    .orElse(null);
            if (possibleValues == null) {
                WynntilsMod.warn("Missing possible values for stat type in item " + itemInfo.getName() + " for stat: "
                        + statType);
                return null;
            }

            return buildUnidentifiedLineParts(itemInfo, style, possibleValues);
        }
    }

    private static Pair<MutableComponent, MutableComponent> buildIdentifiedLineParts(
            IdentifiableItemProperty itemInfo,
            TooltipStyle style,
            StatActualValue actualValue,
            ClassType currentClass,
            MutableComponent suffix) {
        StatType statType = actualValue.statType();
        int value = actualValue.value();

        int valueToShow = statType.calculateAsInverted() ? -value : value;
        boolean hasPositiveEffect = valueToShow > 0 ^ statType.displayAsInverted();
        String starString = style.showStars() ? "***".substring(3 - actualValue.stars()) : "";
        String displayName = Models.Stat.getDisplayName(
                statType, itemInfo.getRequiredClass(), currentClass, itemInfo.getIdentificationLevelRange());

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

        if (!starString.isEmpty()) {
            right.append(Component.literal(starString)
                    .withStyle(Style.EMPTY
                            .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                            .withColor(hasPositiveEffect ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED)));
        }

        if (suffix != null) {
            right.append(suffix);
        }

        return Pair.of(left, right);
    }

    private static Pair<MutableComponent, MutableComponent> buildUnidentifiedLineParts(
            IdentifiableItemProperty itemInfo, TooltipStyle style, StatPossibleValues possibleValues) {
        StatType statType = possibleValues.statType();

        // Determine which value to show first and which to show last in the "A to B"
        // range displayed
        Pair<Integer, Integer> displayRange =
                StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());
        String displayName = Models.Stat.getDisplayName(
                statType,
                itemInfo.getRequiredClass(),
                Models.Character.getClassType(),
                itemInfo.getIdentificationLevelRange());

        // Use displayRange.a as representative; assume both a and b are either < or > 0.
        boolean hasPositiveEffect = displayRange.a() > 0 ^ statType.displayAsInverted();
        ChatFormatting colorCode = hasPositiveEffect ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting colorCodeDark = hasPositiveEffect ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;

        MutableComponent left = Component.empty();
        appendIconPrefix(left, statType);
        left.append(Component.literal(displayName + " ")
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(ChatFormatting.WHITE)));

        MutableComponent right = Component.literal(StringUtils.toSignedString(displayRange.a()))
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(colorCode));
        right.append(Component.literal(" to ")
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(colorCodeDark)));
        right.append(Component.literal(displayRange.b() + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY
                        .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(colorCode)));

        return Pair.of(left, right);
    }

    private static void appendIconPrefix(MutableComponent line, StatType statType) {
        String iconPrefix = getIconPrefix(statType);
        if (iconPrefix.isEmpty()) return;

        line.append(IdentifiableTooltipComponent.withWhiteShadow(Component.literal(iconPrefix)
                .withStyle(Style.EMPTY.withFont(ATTRIBUTE_SPRITE_FONT).withColor(ChatFormatting.WHITE))));
    }

    private static MutableComponent withWynncraftFont(MutableComponent component) {
        return Component.empty()
                .withStyle(Style.EMPTY.withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT))
                .append(component);
    }

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

    private static boolean shouldMarkIdentificationAlignment(IdentifiableItemProperty<?, ?> itemInfo) {
        if (!(itemInfo.getItemInfo() instanceof GearInfo)) {
            return false;
        }

        if (itemInfo instanceof PagedItemProperty pagedItemProperty) {
            return pagedItemProperty.isStatPage();
        }

        return true;
    }
}
