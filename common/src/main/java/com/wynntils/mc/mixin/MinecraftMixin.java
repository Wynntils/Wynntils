/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    private void setScreenPostPost(Screen screen, CallbackInfo ci) {
        EventFactory.onScreenOpened(screen);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickPre(CallbackInfo ci) {
        WynntilsMod.getEventBus().post(new ClientTickEvent(ClientTickEvent.Phase.START));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTickPost(CallbackInfo ci) {
        WynntilsMod.getEventBus().post(new ClientTickEvent(ClientTickEvent.Phase.START));
    }
}
