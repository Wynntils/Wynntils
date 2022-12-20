/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.mc.objects.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ChatPacketReceivedEvent extends Event {
    private final ChatType type;
    private Component message;

    public ChatPacketReceivedEvent(ChatType type, Component message) {
        this.type = type;
        this.message = message;
    }

    public Component getMessage() {
        return message;
    }

    public ChatType getType() {
        return type;
    }

    public void setMessage(Component message) {
        this.message = message;
    }
}
