/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
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
        MixinHelper.post(new PlayerJoinedWorldEvent(player));
    }

    @Inject(method = "disconnect()V", at = @At("HEAD"))
    private void disconnectPre(CallbackInfo ci) {
        // User-triggered logoff
        MixinHelper.post(new ConnectionEvent.DisconnectedEvent());
    }
}
