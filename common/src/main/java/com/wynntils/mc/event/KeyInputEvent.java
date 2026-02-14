/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.input.KeyEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class KeyInputEvent extends Event implements ICancellableEvent {
    private final int action;
    private final KeyEvent keyEvent;

    public KeyInputEvent(KeyEvent keyEvent, int action) {
        this.action = action;
        this.keyEvent = keyEvent;
    }

    public int getAction() {
        return this.action;
    }

    public KeyEvent getKeyEvent() {
        return this.keyEvent;
    }

    public int getKey() {
        return this.keyEvent.key();
    }

    public int getModifiers() {
        return this.keyEvent.modifiers();
    }

    public int getScanCode() {
        return this.keyEvent.scancode();
    }
}
