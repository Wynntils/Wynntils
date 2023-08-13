/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void channelRead0Pre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.PacketReceivedEvent<? extends Packet<?>> event = new PacketEvent.PacketReceivedEvent<>(packet);
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void sendPre(Packet<?> packet, PacketSendListener sendListener, CallbackInfo ci) {
        PacketEvent.PacketSentEvent<? extends Packet<?>> event = new PacketEvent.PacketSentEvent<>(packet);
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
