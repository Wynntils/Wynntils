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

public abstract class ChatMessageEvent extends Event implements ICancellableEvent {
    protected final StyledText message;
    private final MessageType messageType;
    private final RecipientType recipientType;

    protected ChatMessageEvent(StyledText message, MessageType messageType, RecipientType recipientType) {
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

    /**
     * This event is what models and features should use to listen to chat messsages.
     */
    public static class MatchingEvent extends ChatMessageEvent {
        public MatchingEvent(StyledText message, MessageType messageType, RecipientType recipientType) {
            super(message, messageType, recipientType);
        }
    }

    /**
     * This event is what features should use if they want to rewrite chat messsages.
     */
    public static class EditableEvent extends ChatMessageEvent {
        private StyledText editedMessage = null;

        public EditableEvent(StyledText message, MessageType messageType, RecipientType recipientType) {
            super(message, messageType, recipientType);
        }

        public StyledText getMessage() {
            return (editedMessage != null) ? editedMessage : message;
        }

        public void setMessage(StyledText message) {
            this.editedMessage = message;
        }
    }
}
