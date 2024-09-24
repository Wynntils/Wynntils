/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class InventoryResynchronizer {
    protected final AbstractContainerMenu menu;

    protected InventoryResynchronizer(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    public AbstractContainerMenu getMenu() {
        return menu;
    }

    public abstract boolean isValid();

    public abstract void resync();
}
