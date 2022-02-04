/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Final @Shadow Map<UUID, LerpingBossEvent> events;

    @Inject(
            method = "update(Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void updatePre(ClientboundBossEventPacket packet, CallbackInfo ci) {
        // TODO inject into operation somehow
        /*
        // Work around bug in Wynncraft that causes NPEs in Vanilla
        Operation operation = packet.getOperation();
        if (operation != Operation.ADD && operation != Operation.REMOVE) {
            if (!events.containsKey(packet.getId())) {
                ci.cancel();
            }
        }

         */
    }
}
