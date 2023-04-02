/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.type.CodedString;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatMessageReceivedEvent extends Event {
    // These are used to keep the original message so different features don't have to fight over it.
    private final Component originalMessage;
    private final CodedString originalCodedMessage;

    private Component message;
    private CodedString codedMessage;
    private final MessageType messageType;
    private final RecipientType recipientType;

    public ChatMessageReceivedEvent(
            Component message, CodedString codedMessage, MessageType messageType, RecipientType recipientType) {
        this.originalMessage = message;
        this.originalCodedMessage = codedMessage;

        this.message = message;
        this.codedMessage = codedMessage; // message, but as a format-coded string
        this.messageType = messageType;
        this.recipientType = recipientType;
    }

    public Component getMessage() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
        this.codedMessage = ComponentUtils.getCoded(message);
    }

    public CodedString getCodedMessage() {
        return codedMessage;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public Component getOriginalMessage() {
        return originalMessage;
    }

    public CodedString getOriginalCodedMessage() {
        return originalCodedMessage;
    }
}
