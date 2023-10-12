/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class PowderSlotsStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(ItemStack itemStack, WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        return List.of(gearItem.getGearInfo().powderSlots());
    }
}
