/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.features.FixPacketBugsFeature;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Final
    @Shadow
    Map<UUID, LerpingBossEvent> events;

    @Redirect(
            method = "update",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket;dispatch(Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket$Handler;)V"))
    private void updatePre(ClientboundBossEventPacket packet, ClientboundBossEventPacket.Handler handler) {
        FixPacketBugsFeature.fixBossEventPackage(packet, handler, events);
    }
}
