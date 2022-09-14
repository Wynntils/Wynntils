/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class ActionBarMessageUpdateEvent extends Event {
    private String message;

    protected ActionBarMessageUpdateEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Center string for coords, string, etc
    public static class ActionText extends ActionBarMessageUpdateEvent {
        public ActionText(String message) {
            super(message);
        }
    }

    public static class HealthText extends ActionBarMessageUpdateEvent {
        public HealthText(String message) {
            super(message);
        }
    }

    public static class ManaText extends ActionBarMessageUpdateEvent {
        public ManaText(String message) {
            super(message);
        }
    }
}
