/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private Channel channel;

    @Inject(method = "disconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void disconnectPre(Component message, CallbackInfo ci) {
        if (!this.channel.isOpen()) return;

        EventFactory.onDisconnect();
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void channelRead0Pre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (EventFactory.onPacketReceived(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void sendPre(
            Packet<?> packet,
            GenericFutureListener<? extends Future<? super Void>> genericFutureListener,
            CallbackInfo ci) {
        if (EventFactory.onPacketSent(packet).isCanceled()) {
            ci.cancel();
        }
    }
}
