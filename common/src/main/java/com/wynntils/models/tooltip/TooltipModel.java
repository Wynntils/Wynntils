/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.tooltip;

import com.wynntils.core.components.Model;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class TooltipModel extends Model {
    public TooltipModel() {
        super(List.of());
    }

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
