/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ClientsideMessageEvent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @WrapOperation(
            method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void onSystemMessage(ChatComponent instance, Component component, Operation<Void> operation) {
        ClientsideMessageEvent event = new ClientsideMessageEvent(component);
        MixinHelper.post(event);

        if (!event.isCanceled()) {
            operation.call(instance, component);
        }
    }
}
