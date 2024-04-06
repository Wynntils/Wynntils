/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ChatScreenKeyTypedEvent;
import com.wynntils.mc.event.ChatScreenSendEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ChatScreenKeyTypedEvent event = new ChatScreenKeyTypedEvent(keyCode, scanCode, modifiers);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/ChatScreen;handleChatInput(Ljava/lang/String;Z)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"),
            cancellable = true)
    private void onPlayerChatSend(String input, boolean addToRecentChat, CallbackInfoReturnable<Boolean> cir) {
        ChatScreenSendEvent event = new ChatScreenSendEvent(input, addToRecentChat);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
