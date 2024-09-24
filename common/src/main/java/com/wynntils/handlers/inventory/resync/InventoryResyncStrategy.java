/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import net.minecraft.world.inventory.AbstractContainerMenu;

public interface InventoryResyncStrategy {
    InventoryResynchronizer getResynchronizer(AbstractContainerMenu menu);

    static InventoryResyncStrategy chain(InventoryResyncStrategy... strategies) {
        return menu -> {
            for (InventoryResyncStrategy strategy : strategies) {
                InventoryResynchronizer resync = strategy.getResynchronizer(menu);
                if (resync != null) {
                    return resync;
                }
            }
            return null;
        };
    }
}
