/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.events.EventThread;
import com.wynntils.utils.wynn.DialogueUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Gets called whenenver an overlay is sent from Server to Client. <br />
 * (Compass update, HP, NPC dialogue, ...
 * */
@EventThread(EventThread.Type.IO)
public class OverlayDisplayEvent extends Event implements ICancellableEvent {
    private final Component component;
    private final DialogueUtils.Content content;

    public OverlayDisplayEvent(Component component, DialogueUtils.Content content) {
        this.component = component;
        this.content = content;
    }

    public Component getComponent() {
        return this.component;
    }

    public DialogueUtils.Content getContent() {
        return this.content;
    }

    public String getText() {
        return this.content.getText();
    }
}
