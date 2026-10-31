/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.builders.CraftedConsumableTooltipBuilder;
import com.wynntils.handlers.tooltip.builders.CraftedGearTooltipBuilder;
import com.wynntils.handlers.tooltip.builders.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.builders.MountTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipHandler extends Handler {
    // region Identifiable items
    public IdentifiableTooltipBuilder buildNew(
            IdentifiableItemProperty identifiableItem, ItemSourceType sourceType, String source, int page) {
        return IdentifiableTooltipBuilder.buildNewItem(identifiableItem, sourceType, source, page);
    }

    public IdentifiableTooltipBuilder buildFromItemStack(
            ItemStack itemStack, IdentifiableItemProperty identifiableItem) {
        // Tomes do not have pages
        int page = identifiableItem instanceof PagedItemProperty pagedItem ? pagedItem.currentPage() : 0;

        return IdentifiableTooltipBuilder.buildFromItemStack(itemStack, identifiableItem, page);
    }
    // endregion

    // region Crafted items
    public CraftedGearTooltipBuilder buildNew(
            CraftedGearItem craftedGearItem, ItemSourceType sourceType, String source, int page) {
        return CraftedGearTooltipBuilder.buildNewItem(craftedGearItem, sourceType, source, page);
    }

    public CraftedGearTooltipBuilder buildFromItemStack(ItemStack itemStack, CraftedGearItem craftedGearItem) {
        return CraftedGearTooltipBuilder.buildFromItemStack(itemStack, craftedGearItem, craftedGearItem.currentPage());
    }

    public CraftedConsumableTooltipBuilder buildNew(
            CraftedConsumableItem craftedConsumableItem, ItemSourceType sourceType, String source) {
        return CraftedConsumableTooltipBuilder.buildNewItem(craftedConsumableItem, sourceType, source);
    }

    public CraftedConsumableTooltipBuilder buildFromItemStack(
            ItemStack itemStack, CraftedConsumableItem craftedConsumableItem) {
        return CraftedConsumableTooltipBuilder.buildFromItemStack(itemStack, craftedConsumableItem);
    }
    // endregion

    // region Mounts
    public MountTooltipBuilder buildNew(MountItem mountItem, ItemSourceType sourceType, String source) {
        return MountTooltipBuilder.buildNewItem(mountItem, sourceType, source);
    }
    // endregion

    public TooltipOptions getTooltipOptions() {
        ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);

        if (isif == null || !isif.isEnabled()) return TooltipOptions.DEFAULT;

        return isif.getTooltipOptions();
    }

    public List<Component> alignTooltip(List<? extends TooltipElement> elements) {
        return TooltipLayout.alignTooltip(elements);
    }

    public List<Component> alignTooltip(List<? extends TooltipElement> elements, int minimumWidth) {
        return TooltipLayout.alignTooltip(elements, minimumWidth);
    }

    public List<Component> alignTooltip(List<? extends TooltipElement> elements, int minimumWidth, int partSpacing) {
        return TooltipLayout.alignTooltip(elements, minimumWidth, partSpacing);
    }
}
