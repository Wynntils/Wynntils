/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    private void setScreenPost(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            EventFactory.onScreenClose();
        } else {
            EventFactory.onScreenOpenedPost(screen);
        }
    }

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void setScreenPre(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            if (EventFactory.onScreenOpenedPre(screen).isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickPost(CallbackInfo ci) {
        EventFactory.onTick();
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void resizeDisplayPost(CallbackInfo ci) {
        EventFactory.onResizeDisplayPost();
    }
}
