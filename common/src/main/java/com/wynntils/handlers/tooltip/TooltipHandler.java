/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.handlers.tooltip.type.IdentifiableItemInfo;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import net.minecraft.world.item.ItemStack;

public class TooltipHandler extends Handler {
    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public TooltipBuilder buildNew(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        return TooltipBuilder.buildNew(gearInfo, gearInstance, hideUnidentified);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemInfo itemInfo) {
        return TooltipBuilder.fromParsedItemStack(itemStack, itemInfo);
    }
}
