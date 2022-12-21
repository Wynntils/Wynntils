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

    public NpcDialogEvent(Component chatMessage) {
        this.chatMessage = chatMessage;
    }

    public Component getChatMessage() {
        return chatMessage;
    }
}
