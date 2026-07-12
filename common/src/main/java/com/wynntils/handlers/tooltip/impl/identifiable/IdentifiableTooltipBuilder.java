/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> the type of the item info
 * @param <U> the type of the item instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private final IdentifiableItemProperty<T, U> itemInfo;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemInfo, List<Component> header, List<Component> footer, String source) {
        super(header, footer, source);
        this.itemInfo = itemInfo;
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem, String source) {
        return new IdentifiableTooltipBuilder<>(identifiableItem, List.of(), List.of(), source);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildFromItemStack(
            ItemStack itemStack, IdentifiableItemProperty<T, U> identifiableItem, String source) {
        return fromTooltipLines(LoreUtils.getTooltipLines(itemStack), identifiableItem, source);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> fromTooltipLines(
            List<Component> tooltipLines, IdentifiableItemProperty<T, U> identifiableItem) {
        return fromTooltipLines(tooltipLines, identifiableItem, "");
    }

    private static <T, U> IdentifiableTooltipBuilder<T, U> fromTooltipLines(
            List<Component> tooltipLines, IdentifiableItemProperty<T, U> identifiableItem, String source) {
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltipLines);
        return new IdentifiableTooltipBuilder<>(identifiableItem, splitLore.a(), splitLore.b(), source);
    }

    public static IdentifiableTooltipBuilder fromParsedItemStack(
            ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return fromTooltipLines(LoreUtils.getTooltipLines(itemStack), itemInfo);
    }

    @Override
    public List<Component> getTooltipLines(
            ClassType currentClass,
            TooltipStyle style,
            TooltipIdentificationDecorator identificationDecorator,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        List<Component> tooltip = new ArrayList<>(
                super.getTooltipLines(currentClass, style, identificationDecorator, weightSource, weightDecorator));
        decorateTitle(tooltip, identificationDecorator);
        return List.copyOf(tooltip);
    }

    private void decorateTitle(List<Component> tooltip, TooltipIdentificationDecorator decorator) {
        if (decorator == null) return;

        for (int i = 1; i < tooltip.size(); i++) {
            MutableComponent line = tooltip.get(i).copy();
            List<Component> siblings = line.getSiblings();
            for (int j = siblings.size() - 1; j >= 0; j--) {
                Component sibling = siblings.get(j);
                String text = sibling.getString().trim();
                if (!text.equals(itemInfo.getName()) && !text.endsWith(itemInfo.getName())) continue;

                siblings.set(j, decorator.getTitle(sibling));
                tooltip.set(i, line);
                return;
            }
        }
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator, int targetWidth) {
        if (itemInfo instanceof PagedItemProperty pagedItem && !pagedItem.isStatPage()) {
            return List.of();
        }

        return TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style, targetWidth);
    }
}
