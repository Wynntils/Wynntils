/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;

public class ChatMessageAddedEvent extends Event {
    private final Component previousMessage;

    private Component message;

    public ChatMessageAddedEvent(Component message, Component previousMessage) {
        this.message = message;
        this.previousMessage = previousMessage;
    }

    public Component getMessage() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    public Component getPreviousMessage() {
        return previousMessage;
    }
}
