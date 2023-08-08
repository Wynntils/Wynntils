/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatMessageReceivedEvent extends Event {
    // These are used to keep the original message so different features don't have to fight over it.
    private final Component originalMessage;
    private final StyledText originalStyledText;

    private Component message;
    private StyledText styledText;

    private final MessageType messageType;
    private final RecipientType recipientType;

    public ChatMessageReceivedEvent(
            Component message, StyledText styledText, MessageType messageType, RecipientType recipientType) {
        this.originalMessage = message;
        this.originalStyledText = styledText;

        this.message = message;
        this.styledText = styledText; // message, but as a styled string
        this.messageType = messageType;
        this.recipientType = recipientType;
    }

    public void setMessage(Component message) {
        this.message = message;
        this.styledText = StyledText.fromComponent(message);
    }

    public Component getOriginalMessage() {
        return originalMessage;
    }

    public StyledText getOriginalStyledText() {
        return originalStyledText;
    }

    public Component getMessage() {
        return message;
    }

    public StyledText getStyledText() {
        return styledText;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }
}
