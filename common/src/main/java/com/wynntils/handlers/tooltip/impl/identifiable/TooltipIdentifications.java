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
import com.wynntils.models.items.properties.IdentifiableItemProperty;
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
    private static final FontDescription WYNNCRAFT_LANGUAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft"));
    private static final Style SPACING_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("space")))
            .withoutShadow();

    private TooltipIdentifications() {}

    public static List<Component> buildTooltip(
            IdentifiableItemProperty<?, ?> itemInfo,
            ClassType currentClass,
            TooltipIdentificationDecorator decorator,
            TooltipStyle style,
            int targetWidth) {
        List<Object> lineEntries = new ArrayList<>();
        List<Pair<MutableComponent, MutableComponent>> statLines = new ArrayList<>();
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
                    lineEntries.add(Component.empty());
                    delimiterNeeded = false;
                }
            }
            if (!allStats.contains(statType)) continue;

            Pair<MutableComponent, MutableComponent> line =
                    getStatLineParts(statType, itemInfo, currentClass, decorator, style);
            if (line == null) continue;

            lineEntries.add(line);
            statLines.add(line);
            delimiterNeeded = true;
        }

        int targetIdentificationWidth = targetWidth;
        for (Pair<MutableComponent, MutableComponent> statLine : statLines) {
            targetIdentificationWidth = Math.max(
                    targetIdentificationWidth,
                    McUtils.mc().font.width(statLine.a()) + McUtils.mc().font.width(statLine.b()));
        }

        List<Component> identifications = new ArrayList<>();
        for (Object entry : lineEntries) {
            if (entry instanceof Component component) {
                identifications.add(component);
                continue;
            }

            @SuppressWarnings("unchecked")
            Pair<MutableComponent, MutableComponent> line = (Pair<MutableComponent, MutableComponent>) entry;
            MutableComponent identification = Component.empty().append(line.a());
            int currentWidth =
                    McUtils.mc().font.width(line.a()) + McUtils.mc().font.width(line.b());
            if (currentWidth < targetIdentificationWidth) {
                String offset = Managers.Font.calculateOffset(currentWidth, targetIdentificationWidth);
                identification.append(Component.literal(offset).withStyle(SPACING_STYLE));
            }
            identification.append(line.b());
            identifications.add(identification);
        }

        if (!identifications.isEmpty() && identifications.getLast().getString().isEmpty()) {
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
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE)));

        MutableComponent right = Component.literal(
                        StringUtils.toSignedString(value) + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY
                        .withFont(WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(positive ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (suffix != null) right.append(suffix);
        return Pair.of(left, right);
    }

    private static Pair<MutableComponent, MutableComponent> buildUnidentifiedLineParts(
            IdentifiableItemProperty<?, ?> itemInfo, TooltipStyle style, StatPossibleValues possibleValues) {
        StatType statType = possibleValues.statType();
        Pair<Integer, Integer> range = StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());
        boolean positive = range.a() > 0 ^ statType.displayAsInverted();
        ChatFormatting color = positive ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting darkColor = positive ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;
        String displayName = Models.Stat.getDisplayName(
                statType,
                itemInfo.getRequiredClass(),
                Models.Character.getClassType(),
                itemInfo.getIdentificationLevelRange());

        MutableComponent left = Component.literal(displayName + " ")
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE));
        MutableComponent right = Component.literal(StringUtils.toSignedString(range.a()))
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(color));
        right.append(Component.literal(" to ")
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(darkColor)));
        right.append(Component.literal(range.b() + statType.getUnit().getDisplayName())
                .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(color)));
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
                        .withFont(ATTRIBUTE_SPRITE_FONT)
                        .withColor(ChatFormatting.WHITE)
                        .withShadowColor(0xFFFFFF)));
    }
}
