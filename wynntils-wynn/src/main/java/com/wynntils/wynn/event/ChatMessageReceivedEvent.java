/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.mc.utils.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatMessageReceivedEvent extends Event {
    private Component message;
    private String codedMessage;
    private final MessageType messageType;
    private final RecipientType recipientType;

    public ChatMessageReceivedEvent(
            Component message, String codedMessage, MessageType messageType, RecipientType recipientType) {
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

    public String getCodedMessage() {
        return codedMessage;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }
}
