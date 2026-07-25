/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public class AverageDpsStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return Optional.empty();
        if (!gearItem.getGearType().isWeapon()) return Optional.empty();

        return Optional.of(gearItem.getItemInfo().fixedStats().averageDps());
    }

    @Override
    public Optional<RangedValue> getExpectedRange() {
        return Optional.of(RangedValue.of(1, 2000));
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }
}
