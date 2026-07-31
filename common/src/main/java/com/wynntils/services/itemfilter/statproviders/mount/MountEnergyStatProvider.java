/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.mount;

import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class MountEnergyStatProvider extends BaseMountStatProvider<CappedValue> {
    @Override
    public Optional<CappedValue> getValue(MountItem mountItem) {
        return Optional.of(mountItem.getMountInfo().currentEnergy());
    }
}
