/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.Objects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow
    protected abstract void sendChat(String plain, Component decorated);

    @Inject(method = "chatSigned", at = @At("HEAD"), cancellable = true)
    private void onChatPre(String plain, Component decorated, CallbackInfo ci) {
        ChatSentEvent result = EventFactory.onChatSent(plain);
        if (result.isCanceled()) {
            ci.cancel();
        }

        if (!Objects.equals(plain, result.getMessage())) {
            this.sendChat(result.getMessage(), decorated);
            ci.cancel();
        }
    }

    @Inject(method = "commandSigned", at = @At("HEAD"), cancellable = true)
    private void onSignedCommandPre(String plain, Component decorated, CallbackInfo ci) {
        if (EventFactory.onCommandSent(plain, true).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "commandUnsigned", at = @At("HEAD"), cancellable = true)
    private void onUnsignedCommandPre(String command, CallbackInfoReturnable<Boolean> cir) {
        if (EventFactory.onCommandSent(command, false).isCanceled()) {
            cir.cancel();
        }
    }

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void onDropPre(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        if (EventFactory.onDropPre(fullStack).isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(Component component, CallbackInfo ci) {
        if ((Object) this != McUtils.player()) return;

        if (EventFactory.onClientsideMessage(component).isCanceled()) {
            ci.cancel();
        }
    }
}
