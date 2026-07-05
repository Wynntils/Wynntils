/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipBuilder;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipBuilder;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import net.minecraft.world.item.ItemStack;

public final class TooltipHandler extends Handler {
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
}
