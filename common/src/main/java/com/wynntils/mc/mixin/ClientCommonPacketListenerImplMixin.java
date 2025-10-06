/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.components.Managers;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected Connection connection;

    @Inject(
            method =
                    "handleResourcePackPush(Lnet/minecraft/network/protocol/common/ClientboundResourcePackPushPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleResourcePackPushPre(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        ServerResourcePackEvent.Load event =
                new ServerResourcePackEvent.Load(packet.id(), packet.url(), packet.hash(), packet.required());
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            // Use the common packet listener's connection, as it's too early to get the player's connection
            this.connection.send(new ServerboundResourcePackPacket(
                    packet.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            ci.cancel();
        }
    }

    @Inject(
            method = "handleDisconnect(Lnet/minecraft/network/protocol/common/ClientboundDisconnectPacket;)V",
            at = @At("HEAD"))
    private void handleDisconnectPre(ClientboundDisconnectPacket packet, CallbackInfo ci) {
        // Unexpected disconnect.
        // NOTE: This will happen on a Netty thread instead of the main thread, but all other
        // ConnectionEvents are sent on the main thread, so let's do so with this one too.
        if (Managers.TickScheduler == null) return;

        Managers.TickScheduler.scheduleNextTick(
                () -> MixinHelper.postAlways(new ConnectionEvent.DisconnectedEvent("disconnect.packet.listener")));
    }
}
