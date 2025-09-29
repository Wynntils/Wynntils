/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ClientsideMessageEvent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @WrapMethod(
            method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V")
    private void handleSystemMessageWrap(
            Component message, boolean overlay, Operation<Void> original) {
        ChatPacketReceivedEvent event = overlay
                ? new ChatPacketReceivedEvent.GameInfoReceivedEvent(message)
                : new ChatPacketReceivedEvent.ChatReceivedEvent(message);
        MixinHelper.post(event);

        Component newMessage = event.isMessageChanged() ? event.getMessage() : message;

        ClientsideMessageEvent event2 = new ClientsideMessageEvent(newMessage);
        MixinHelper.post(event);

        if (!event.isCanceled() && !event2.isCanceled()) {
            original.call(newMessage, overlay);
        }
    }
}
