/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.event;

import com.wynntils.handlers.inventory.InventoryInteraction;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.Event;

public class InventoryInteractionEvent extends Event {
    private final AbstractContainerMenu menu;
    private final InventoryInteraction interaction;

    public InventoryInteractionEvent(AbstractContainerMenu menu, InventoryInteraction interaction) {
        this.menu = menu;
        this.interaction = interaction;
    }

    public AbstractContainerMenu getMenu() {
        return menu;
    }

    public InventoryInteraction getInteraction() {
        return interaction;
    }
}
