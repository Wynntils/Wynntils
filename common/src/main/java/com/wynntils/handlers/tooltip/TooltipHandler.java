/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipBuilder;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.HashMap;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipHandler extends Handler {
    public List<Component> updateTooltip(
            List<Component> originalLines, IdentifiableItemProperty<?, ?> identifiableItem, TooltipOptions options) {
        if (identifiableItem.getIdentifications().isEmpty()) return originalLines;

        if (!(identifiableItem instanceof WynnItem wynnItem)) {
            return calculateUpdatedTooltip(originalLines, identifiableItem, options);
        }

        Object cachedTooltip = wynnItem.getData().get(WynnItemData.TOOLTIP_KEY);
        if (cachedTooltip instanceof TooltipBuilder) {
            return originalLines;
        }

        UpdatedTooltipCache cache;
        if (cachedTooltip instanceof UpdatedTooltipCache updatedTooltipCache) {
            cache = updatedTooltipCache;
        } else {
            cache = new UpdatedTooltipCache();
            wynnItem.getData().store(WynnItemData.TOOLTIP_KEY, cache);
        }

        UpdateKey key = new UpdateKey(List.copyOf(originalLines), options);
        return cache.computeIfAbsent(key, ignored -> calculateUpdatedTooltip(originalLines, identifiableItem, options));
    }

    private List<Component> calculateUpdatedTooltip(
            List<Component> originalLines, IdentifiableItemProperty<?, ?> identifiableItem, TooltipOptions options) {
        return IdentifiableTooltipBuilder.fromTooltipLines(originalLines, identifiableItem)
                .getTooltipLines(Models.Character.getClassType(), options);
    }

    public IdentifiableTooltipBuilder buildNew(
            IdentifiableItemProperty identifiableItem, boolean hideUnidentified, boolean showItemType) {
        return buildNew(identifiableItem, hideUnidentified, showItemType, "");
    }

    public IdentifiableTooltipBuilder buildNew(
            IdentifiableItemProperty identifiableItem, boolean hideUnidentified, boolean showItemType, String source) {
        return IdentifiableTooltipBuilder.buildNewItem(identifiableItem, source);
    }

    public IdentifiableTooltipBuilder buildFromItemStack(
            ItemStack itemStack,
            IdentifiableItemProperty identifiableItem,
            boolean hideUnidentified,
            boolean showItemType,
            String source) {
        return IdentifiableTooltipBuilder.buildFromItemStack(itemStack, identifiableItem, source);
    }

    public CraftedTooltipBuilder buildNew(CraftedItemProperty craftedItemProperty, String source) {
        return CraftedTooltipBuilder.buildNewItem(craftedItemProperty, source);
    }

    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return IdentifiableTooltipBuilder.fromParsedItemStack(itemStack, itemInfo);
    }

    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItemProperty) {
        return CraftedTooltipBuilder.fromParsedItemStack(itemStack, craftedItemProperty);
    }

    private record UpdateKey(List<Component> originalLines, TooltipOptions options) {}

    private static final class UpdatedTooltipCache extends HashMap<UpdateKey, List<Component>> {}
}
