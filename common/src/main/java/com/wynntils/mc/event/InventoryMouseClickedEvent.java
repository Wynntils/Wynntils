/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class InventoryMouseClickedEvent extends Event {
    private final double mouseX;
    private final double mouseY;
    private final int button;
    private final Slot hoveredSlot;

    public InventoryMouseClickedEvent(double mouseX, double mouseY, int button, Slot hoveredSlot) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.hoveredSlot = hoveredSlot;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public int getButton() {
        return button;
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
