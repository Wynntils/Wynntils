/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.PacketEvent;
import com.wynntils.utils.mc.McUtils;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private volatile PacketListener packetListener;

    @Unique
    private static boolean isRenderThread() {
        return McUtils.mc().isSameThread();
    }

    @WrapMethod(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V")
    private void channelRead0Pre(
            ChannelHandlerContext channelHandlerContext, Packet<?> packet, Operation<Void> original) {
        Packet<?> transformedPacket = handleReceivePacket(packet);

        // Cancel send if packet is cancelled
        if (transformedPacket == null) return;

        original.call(channelHandlerContext, packet);
    }

    @Unique
    private static Packet<?> handleReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundBundlePacket bundlePacket) {
            List<Packet<? super ClientGamePacketListener>> remainingPackets = new ArrayList<>();
            boolean hasCancelled = false;
            for (Packet<? super ClientGamePacketListener> bundledPacket : bundlePacket.subPackets()) {
                PacketEvent.PacketSentEvent<? extends Packet<?>> bundledEvent =
                        new PacketEvent.PacketSentEvent<>(bundledPacket);
                MixinHelper.postAlways(bundledEvent);
                if (!bundledEvent.isCanceled()) {
                    remainingPackets.add(bundledPacket);
                } else {
                    hasCancelled = true;
                }
            }
            if (hasCancelled) {
                // Some packets were removed, recreate the bundle with the remaining packets,
                // or cancel if all packets are removed
                if (remainingPackets.isEmpty()) {
                    return null;
                }
                return new ClientboundBundlePacket(remainingPackets);
            }
        } else {
            PacketEvent.PacketReceivedEvent<? extends Packet<?>> event = new PacketEvent.PacketReceivedEvent<>(packet);
            MixinHelper.postAlways(event);
            if (event.isCanceled()) {
                return null;
            }
        }
        return packet;
    }

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("RETURN"))
    private void disconnectPost(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        if (!(this.packetListener instanceof ClientPacketListener)) return;
        if (!isRenderThread()) return;

        String reason = disconnectionDetails.reason().getContents() instanceof TranslatableContents tc
                ? tc.getKey()
                : "unknown";
        ConnectionEvent.DisconnectedEvent event = new ConnectionEvent.DisconnectedEvent(reason);
        MixinHelper.postAlways(event);
    }

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
            at = @At("HEAD"),
            cancellable = true)
    private void sendPre(Packet<?> packet, PacketSendListener sendListener, boolean flush, CallbackInfo ci) {
        PacketEvent.PacketSentEvent<? extends Packet<?>> event = new PacketEvent.PacketSentEvent<>(packet);
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
