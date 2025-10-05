/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.input.KeyEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ChatScreenKeyTypedEvent extends Event implements ICancellableEvent {
    private final KeyEvent keyEvent;

    public ChatScreenKeyTypedEvent(KeyEvent keyEvent) {
        this.keyEvent = keyEvent;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    public int getKeyCode() {
        return keyEvent.input();
    }

    public int getScanCode() {
        return keyEvent.scancode();
    }

    public int getModifiers() {
        return keyEvent.modifiers();
    }
}
