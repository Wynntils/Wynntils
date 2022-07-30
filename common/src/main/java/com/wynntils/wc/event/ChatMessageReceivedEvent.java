/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatMessageReceivedEvent extends Event {
    private Component message;
    private final MessageType type;

    public ChatMessageReceivedEvent(Component message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public Component getMessage() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public enum MessageType {
        NORMAL,
        SYSTEM,
        BACKGROUND
    }
}
