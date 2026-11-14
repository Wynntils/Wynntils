/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Fired before a user opens the content book. Background queries do not fire this event. */
public class ContentBookOpenEvent extends Event implements ICancellableEvent {
    private final OpenAction action;

    public ContentBookOpenEvent() {
        this(OpenAction.PROGRAMMATIC);
    }

    public ContentBookOpenEvent(OpenAction action) {
        this.action = action;
    }

    public OpenAction getAction() {
        return action;
    }

    public enum OpenAction {
        PROGRAMMATIC(false),
        LEFT_CLICK(false),
        SHIFT_LEFT_CLICK(true),
        RIGHT_CLICK(false),
        SHIFT_RIGHT_CLICK(true),
        INVENTORY_CLICK(false),
        SHIFT_INVENTORY_CLICK(true);

        private final boolean shift;

        OpenAction(boolean shift) {
            this.shift = shift;
        }

        public boolean isShift() {
            return shift;
        }
    }
}
