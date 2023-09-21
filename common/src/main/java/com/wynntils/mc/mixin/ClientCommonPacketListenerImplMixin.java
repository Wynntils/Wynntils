/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Inject(
            method = "handleResourcePack(Lnet/minecraft/network/protocol/common/ClientboundResourcePackPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleResourcePackPre(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        ResourcePackEvent event = new ResourcePackEvent(packet.getUrl(), packet.getHash(), packet.isRequired());
        MixinHelper.post(event);
        if (event.isCanceled()) {
            McUtils.sendPacket(
                    new ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            ci.cancel();
        }
    }

    @Inject(
            method = "handleDisconnect(Lnet/minecraft/network/protocol/common/ClientboundDisconnectPacket;)V",
            at = @At("HEAD"))
    private void handleDisconnectPre(ClientboundDisconnectPacket packet, CallbackInfo ci) {
        // Unexpected disconnect
        MixinHelper.post(new ConnectionEvent.DisconnectedEvent());
    }
}
