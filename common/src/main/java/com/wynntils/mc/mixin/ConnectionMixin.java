/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "disconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("RETURN"))
    private void disconnectPost(Component message, CallbackInfo ci) {
        EventFactory.onDisconnect();
    }

    @Inject(
            method = "connectToServer(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/Connection;",
            at = @At("RETURN"))
    private static void connectToServerPost(
            InetAddress inetAddress,
            int port,
            boolean useEpoll,
            CallbackInfoReturnable<Connection> cir) {
        EventFactory.onConnect(inetAddress, port);
    }

    @Inject(
            method =
                    "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("RETURN"))
    private void channelRead0Post(
            ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        EventFactory.onPacketReceived(packet);
    }

    @Inject(
            method =
                    "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("RETURN"))
    private void sendPost(
            Packet<?> packet,
            GenericFutureListener<? extends Future<? super Void>> genericFutureListener,
            CallbackInfo ci) {
        EventFactory.onPacketSent(packet);
    }
}
