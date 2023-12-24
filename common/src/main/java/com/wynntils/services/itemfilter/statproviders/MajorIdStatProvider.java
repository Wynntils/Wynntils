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

public class MajorIdStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        return gearItem.getItemInfo().fixedStats().majorIds().stream()
                .map(GearMajorId::name)
                .toList();
    }
}
