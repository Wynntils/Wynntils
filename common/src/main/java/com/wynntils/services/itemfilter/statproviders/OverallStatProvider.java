/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class OverallStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof GearItem gearItem) {
            Optional<GearInstance> gearInstanceOpt = gearItem.getGearInstance();

            if (gearInstanceOpt.isEmpty()) {
                return List.of();
            }

            return List.of((int) gearInstanceOpt.get().getOverallPercentage());
        }

        return List.of();
    }
}
