/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class InventoryMouseClickedEvent extends Event implements ICancellableEvent {
    private final MouseButtonEvent mouseButtonEvent;
    private final boolean isDoubleClick;
    private final Slot hoveredSlot;

    public InventoryMouseClickedEvent(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick, Slot hoveredSlot) {
        this.mouseButtonEvent = mouseButtonEvent;
        this.isDoubleClick = isDoubleClick;
        this.hoveredSlot = hoveredSlot;
    }

    public MouseButtonEvent getMouseButtonEvent() {
        return mouseButtonEvent;
    }

    public double getMouseX() {
        return mouseButtonEvent.x();
    }

    public double getMouseY() {
        return mouseButtonEvent.y();
    }

    public int getButton() {
        return mouseButtonEvent.button();
    }

    public boolean isDoubleClick() {
        return isDoubleClick;
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
