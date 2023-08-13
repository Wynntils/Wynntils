/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatSentEvent extends Event {
    private final String message;

    public ChatSentEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
