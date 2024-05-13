/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class MajorIdStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return Optional.empty();

        return gearItem.getItemInfo().fixedStats().majorIds().stream()
                .map(GearMajorId::name)
                .findFirst();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }
}
