/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class InventoryMouseDraggedEvent extends Event {
    private final double mouseX;
    private final double mouseY;
    private final int button;
    private final double deltaX;
    private final double deltaY;

    public InventoryMouseDraggedEvent(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
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

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }
}
