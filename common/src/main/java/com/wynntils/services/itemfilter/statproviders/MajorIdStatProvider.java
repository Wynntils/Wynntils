/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class MajorIdStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(ItemStack itemStack, WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        return gearItem.getGearInfo().fixedStats().majorIds().stream()
                .map(GearMajorId::name)
                .toList();
    }
}
