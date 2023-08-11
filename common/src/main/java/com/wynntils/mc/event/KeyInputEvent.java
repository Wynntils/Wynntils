/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class KeyInputEvent extends Event {
    private final int action;
    private final int key;
    private final int modifiers;
    private final int scanCode;

    public KeyInputEvent(int key, int scanCode, int action, int modifiers) {
        this.action = action;
        this.key = key;
        this.modifiers = modifiers;
        this.scanCode = scanCode;
    }

    public int getAction() {
        return this.action;
    }

    public int getKey() {
        return this.key;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    public int getScanCode() {
        return this.scanCode;
    }
}
