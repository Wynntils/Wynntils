/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mixin;

import com.wynntils.mc.event.EventFactory;
import java.net.InetAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "disconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("RETURN"))
    public void disconnectPost(Component message, CallbackInfo ci) {
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
}
