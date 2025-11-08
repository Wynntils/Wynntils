/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.SystemMessageEvent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @WrapMethod(method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V")
    private void handleSystemMessageWrap(Component message, boolean overlay, Operation<Void> original) {
        SystemMessageEvent event = overlay
                ? new SystemMessageEvent.GameInfoReceivedEvent(message)
                : new SystemMessageEvent.ChatReceivedEvent(message);
        MixinHelper.post(event);

        Component newMessage = event.isMessageChanged() ? event.getMessage() : message;

        if (!event.isCanceled()) {
            original.call(newMessage, overlay);
        }
    }
}
