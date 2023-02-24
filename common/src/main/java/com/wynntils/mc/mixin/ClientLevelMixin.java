/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "addPlayer(ILnet/minecraft/client/player/AbstractClientPlayer;)V", at = @At("HEAD"))
    private void addPlayer(int id, AbstractClientPlayer player, CallbackInfo ci) {
        EventFactory.onPlayerJoinedWorld(player);
    }

    @Inject(method = "disconnect()V", at = @At("HEAD"))
    private void disconnectPre(CallbackInfo ci) {
        // User-triggered logoff
        EventFactory.onDisconnect();
    }
}
