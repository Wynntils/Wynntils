/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NpcDialogEvent extends Event {
    private final Component chatMessage;
    private final boolean needsConfirmation;

    public NpcDialogEvent(Component chatMessage, boolean needsConfirmation) {
        this.chatMessage = chatMessage;
        this.needsConfirmation = needsConfirmation;
    }

    /**
     * Return true if this message needs to be confirmed using the sneak key to
     * progress the dialogue.
     */
    public boolean needsConfirmation() {
        return needsConfirmation;
    }

    public Component getChatMessage() {
        return chatMessage;
    }
}
