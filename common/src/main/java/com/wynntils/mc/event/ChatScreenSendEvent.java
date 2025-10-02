/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class ChatScreenSendEvent extends BaseEvent implements ICancellableEvent {
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
