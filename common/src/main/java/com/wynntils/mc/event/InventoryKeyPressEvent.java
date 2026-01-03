/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class InventoryKeyPressEvent extends Event implements ICancellableEvent {
    private final KeyEvent keyEvent;
    private final Slot hoveredSlot;

    public InventoryKeyPressEvent(KeyEvent keyEvent, Slot hoveredSlot) {
        this.keyEvent = keyEvent;
        this.hoveredSlot = hoveredSlot;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    public int getKeyCode() {
        return keyEvent.key();
    }

    public int getScanCode() {
        return keyEvent.scancode();
    }

    public int getModifiers() {
        return keyEvent.modifiers();
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
