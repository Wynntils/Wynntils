/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import net.minecraft.world.item.ItemStack;

public class TooltipHandler extends Handler {
    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public TooltipBuilder buildNew(GearItem gearItem, boolean hideUnidentified) {
        return TooltipBuilder.buildNewGear(gearItem, hideUnidentified);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return TooltipBuilder.fromParsedItemStack(itemStack, itemInfo);
    }
}
