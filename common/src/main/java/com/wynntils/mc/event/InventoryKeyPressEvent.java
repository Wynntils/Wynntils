/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.world.inventory.Slot;

public class InventoryKeyPressEvent extends BaseEvent implements OperationCancelable {
    private final int keyCode;
    private final int scanCode;
    private final int modifiers;
    private final Slot hoveredSlot;

    public InventoryKeyPressEvent(int keyCode, int scanCode, int modifiers, Slot hoveredSlot) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.hoveredSlot = hoveredSlot;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getScanCode() {
        return scanCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
