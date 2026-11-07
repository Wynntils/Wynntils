/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ChatComponentClickedEvent extends Event implements ICancellableEvent {
    private final Style clicked;
    private final boolean allowInsertions;

    public ChatComponentClickedEvent(Style clicked, boolean allowInsertions) {
        this.clicked = clicked;
        this.allowInsertions = allowInsertions;
    }

    public Style getClicked() {
        return clicked;
    }

    public boolean allowsInsertions() {
        return allowInsertions;
    }
}
