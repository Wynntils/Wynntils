/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ChatSentEvent extends WynntilsEvent {
    private final String message;

    public ChatSentEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
