/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class InventoryKeyPressEvent extends Event implements ICancellableEvent {
    private final KeyEvent keyEvent;
    private final SlotWidget hoveredSlot;

    public InventoryKeyPressEvent(KeyEvent keyEvent, SlotWidget hoveredSlot) {
        this.keyEvent = keyEvent;
        this.hoveredSlot = hoveredSlot;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    public int getKey() {
        return keyEvent.key();
    }

    public int getKeyCode() {
        return keyEvent.keycode();
    }

    public int getModifiers() {
        return keyEvent.modifiers();
    }

    public SlotWidget getHoveredSlotWidget() {
        return hoveredSlot;
    }

    public Slot getHoveredSlot() {
        return hoveredSlot == null ? null : hoveredSlot.slot();
    }
}
