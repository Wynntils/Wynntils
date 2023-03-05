/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.DropHeldItemEvent;
import com.wynntils.utils.mc.McUtils;
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
        if (MixinHelper.post(new DropHeldItemEvent(fullStack)).isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(Component component, CallbackInfo ci) {
        if ((Object) this != McUtils.player()) return;

        if (MixinHelper.post(new ClientsideMessageEvent(component)).isCanceled()) {
            ci.cancel();
        }
    }
}
