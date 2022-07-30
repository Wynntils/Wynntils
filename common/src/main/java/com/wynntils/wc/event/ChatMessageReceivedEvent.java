/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.event;

import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatMessageReceivedEvent extends Event {
    private Component message;
    private final MessageType type;
    private final RecipientType recipient;

    public ChatMessageReceivedEvent(Component message, MessageType type, RecipientType recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
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

    public RecipientType getRecipient() {
        return recipient;
    }
}
