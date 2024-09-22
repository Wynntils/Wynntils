/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class HealthStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof GearItem gearItem) {
            return Optional.of(gearItem.getItemInfo().fixedStats().healthBuff());
        }

        if (wynnItem instanceof CraftedGearItem craftedGearItem) {
            return Optional.of(craftedGearItem.getHealth());
        }

        return Optional.empty();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR, ItemProviderType.GEAR_INSTANCE);
    }
}
