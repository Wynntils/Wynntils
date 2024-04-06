/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatScreenSendEvent extends Event {
    private final String input;
    private final boolean addToRecentChat;

    public ChatScreenSendEvent(String input, boolean addToRecentChat) {
        this.input = input;
        this.addToRecentChat = addToRecentChat;
    }

    public String getInput() {
        return input;
    }

    public boolean isAddToRecentChat() {
        return addToRecentChat;
    }
}
