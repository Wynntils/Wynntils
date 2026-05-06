/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private static final FontDescription IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));

    private final CraftedItemProperty craftedItem;
    private final CraftedTooltipComponent tooltipComponent;
    private final Map<StatType, Component> originalRollWheelSuffixes;

    private CraftedTooltipBuilder(
            CraftedItemProperty craftedItem,
            List<Component> header,
            List<Component> footer,
            String source,
            CraftedTooltipComponent tooltipComponent,
            Map<StatType, Component> originalRollWheelSuffixes) {
        super(header, footer, source);
        this.craftedItem = craftedItem;
        this.tooltipComponent = tooltipComponent;
        this.originalRollWheelSuffixes = originalRollWheelSuffixes;
    }

    private CraftedTooltipBuilder(CraftedItemProperty craftedItem, List<Component> header, List<Component> footer) {
        this(craftedItem, header, footer, "", null, Map.of());
    }

    public static <T extends CraftedItemProperty> CraftedTooltipBuilder buildNewItem(
            T craftedItem, CraftedTooltipComponent<T> tooltipComponent, String source) {
        List<Component> header = tooltipComponent.buildHeaderTooltip(craftedItem);
        List<Component> footer = tooltipComponent.buildFooterTooltip(craftedItem);
        return new CraftedTooltipBuilder(craftedItem, header, footer, source, tooltipComponent, Map.of());
    }

    public static <T extends CraftedItemProperty> CraftedTooltipBuilder buildFromItemStack(
            ItemStack itemStack, T craftedItem, CraftedTooltipComponent<T> tooltipComponent) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);
        Map<StatType, Component> originalRollWheelSuffixes = extractOriginalRollWheelSuffixes(tooltips);
        CraftedTooltipComponent.TooltipParts tooltipParts = tooltipComponent.buildTooltipParts(itemStack, craftedItem);
        if (tooltipParts != null) {
            return new CraftedTooltipBuilder(
                    craftedItem,
                    tooltipParts.header(),
                    tooltipParts.footer(),
                    "",
                    tooltipComponent,
                    originalRollWheelSuffixes);
        }

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new CraftedTooltipBuilder(
                craftedItem, splitLore.a(), splitLore.b(), "", tooltipComponent, originalRollWheelSuffixes);
    }

    public static CraftedTooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new CraftedTooltipBuilder(craftedItem, header, footer);
    }

    @Override
    protected List<Component> getWeightedHeaderLines(
            List<Component> originalHeader,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipStyle style) {
        // Crafted items do not have weighting
        return originalHeader;
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator, int targetWidth) {
        return CraftedTooltipIdentifications.buildTooltip(craftedItem, currentClass, style, originalRollWheelSuffixes);
    }

    @Override
    protected List<Component> postProcessTooltipLines(List<Component> tooltip, int targetWidth) {
        if (tooltipComponent == null) {
            return tooltip;
        }

        return tooltipComponent.finalizeTooltipLines(tooltip, targetWidth, craftedItem);
    }

    private static Map<StatType, Component> extractOriginalRollWheelSuffixes(List<Component> tooltipLines) {
        Map<StatType, Component> rollWheelSuffixes = new HashMap<>();

        for (Component tooltipLine : tooltipLines) {
            StyledText normalizedLine = StyledText.fromComponent(tooltipLine).getNormalized();
            Matcher matcher = normalizedLine.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN);
            if (!matcher.matches()) {
                continue;
            }

            StatType statType = Models.Stat.fromDisplayName(matcher.group("statName"), matcher.group("unit"));
            if (statType == null) {
                continue;
            }

            Component trailingRollWheel =
                    TooltipUtils.extractTrailingSegmentWithFont(tooltipLine, IDENTIFICATION_METER_FONT);
            if (trailingRollWheel != null) {
                rollWheelSuffixes.put(statType, trailingRollWheel);
            }
        }

        return rollWheelSuffixes;
    }
}
