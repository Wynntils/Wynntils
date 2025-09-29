/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import net.neoforged.bus.api.Event;

/**
 * This event is what most models and features should use if they want to rewrite chat messsages.
 */
public class ChatMessageRewriteEvent extends Event {
    private StyledText message;
    private final MessageType messageType;
    private final RecipientType recipientType;

    public ChatMessageRewriteEvent(StyledText message, MessageType messageType, RecipientType recipientType) {
        this.message = message;
        this.messageType = messageType;
        this.recipientType = recipientType;
    }

    public StyledText getMessage() {
        return message;
    }

    public void setMessage(StyledText message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }
}
