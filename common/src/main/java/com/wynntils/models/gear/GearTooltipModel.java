/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.core.components.Model;
import com.wynntils.models.gear.tooltip.GearTooltipBuilder;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class GearTooltipModel extends Model {
    public GearTooltipModel() {
        super(List.of());
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public GearTooltipBuilder buildNew(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        return GearTooltipBuilder.buildNew(gearInfo, gearInstance, hideUnidentified);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public GearTooltipBuilder fromParsedItemStack(ItemStack itemStack, GearItem gearItem) {
        return GearTooltipBuilder.fromParsedItemStack(itemStack, gearItem);
    }
}
