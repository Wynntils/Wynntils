/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.components.Managers;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.CommandsAddedEvent;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.mixin.accessors.CommandBoxAccessor;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
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
            method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V",
            at = @At("RETURN"))
    private void handleCommandsPost(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!McUtils.mc().isSameThread()) return;
        if (!((Object) this instanceof ClientPacketListener listener)) return;

        // Use the installed tree so commands added by other mods are preserved.
        var root = ((CommandBoxAccessor) listener.commandSuggestions())
                .getDispatcher()
                .getRoot();
        MixinHelper.post(new CommandsAddedEvent(
                root,
                CommandBuildContext.simple(listener.getSuggestionsProvider().enabledFeatures())));
    }

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
