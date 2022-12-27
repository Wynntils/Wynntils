/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.handlers.chat.NpcDialogueType;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NpcDialogEvent extends Event {
    private final List<Component> chatMessage;
    private final NpcDialogueType type;

    public NpcDialogEvent(List<Component> chatMessage, NpcDialogueType type) {
        this.chatMessage = chatMessage;
        this.type = type;
    }

    public NpcDialogueType getType() {
        return type;
    }

    public List<Component> getChatMessage() {
        return chatMessage;
    }
}
