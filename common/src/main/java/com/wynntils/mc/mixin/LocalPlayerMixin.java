/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
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
