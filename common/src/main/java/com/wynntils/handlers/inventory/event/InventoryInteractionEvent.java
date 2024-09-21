/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.event;

import com.wynntils.handlers.inventory.InventoryInteraction;
import com.wynntils.utils.type.Confidence;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.Event;

public class InventoryInteractionEvent extends Event {
    private final AbstractContainerMenu menu;
    private final InventoryInteraction interaction;
    private final Confidence confidence;

    public InventoryInteractionEvent(
            AbstractContainerMenu menu, InventoryInteraction interaction, Confidence confidence) {
        this.menu = menu;
        this.interaction = interaction;
        this.confidence = confidence;
    }

    public AbstractContainerMenu getMenu() {
        return menu;
    }

    public InventoryInteraction getInteraction() {
        return interaction;
    }

    public Confidence getConfidence() {
        return confidence;
    }
}
