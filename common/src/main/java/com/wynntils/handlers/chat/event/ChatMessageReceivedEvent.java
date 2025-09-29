/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is what models and features should use to listen to chat messsages.
 */
public class ChatMessageReceivedEvent extends Event implements ICancellableEvent {
    private final StyledText message;
    private final MessageType messageType;
    private final RecipientType recipientType;

    public ChatMessageReceivedEvent(StyledText message, MessageType messageType, RecipientType recipientType) {
        this.message = message;
        this.messageType = messageType;
        this.recipientType = recipientType;
    }

    public StyledText getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }
}
