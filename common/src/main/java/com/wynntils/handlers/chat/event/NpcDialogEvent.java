/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.event;

import com.wynntils.handlers.chat.type.NpcDialogueType;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NpcDialogEvent extends Event {
    private final List<Component> chatMessage;
    private final NpcDialogueType type;
    private final boolean isProtected;

    public NpcDialogEvent(List<Component> chatMessage, NpcDialogueType type, boolean isProtected) {
        this.chatMessage = chatMessage;
        this.type = type;
        this.isProtected = isProtected;
    }

    public NpcDialogueType getType() {
        return type;
    }

    public List<Component> getChatMessage() {
        return chatMessage;
    }

    public boolean isProtected() {
        return isProtected;
    }
}
