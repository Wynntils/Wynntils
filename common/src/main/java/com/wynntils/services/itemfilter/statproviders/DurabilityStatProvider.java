/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class DurabilityStatProvider extends ItemStatProvider<CappedValue> {
    @Override
    public List<CappedValue> getValue(ItemStack itemStack, WynnItem wynnItem) {
        if (!(wynnItem instanceof DurableItemProperty durableItemProperty)) return List.of();

        return List.of(durableItemProperty.getDurability());
    }
}
