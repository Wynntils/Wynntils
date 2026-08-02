/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.mount;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public abstract class BaseMountStatProvider<T extends Comparable<T>> extends ItemStatProvider<T> {
    @Override
    public final Optional<T> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof MountItem mountItem) {
            return getValue(mountItem);
        }

        return Optional.empty();
    }

    public abstract Optional<T> getValue(MountItem mountItem);

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.MOUNT);
    }
}
