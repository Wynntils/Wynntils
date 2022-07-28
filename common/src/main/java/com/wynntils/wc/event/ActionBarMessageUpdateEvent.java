/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.event;

import net.minecraftforge.eventbus.api.Event;

public class ActionBarMessageUpdateEvent extends Event {
    private final String message;

    public ActionBarMessageUpdateEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
