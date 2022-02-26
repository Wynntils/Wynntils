/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.wynntils.core.commands.ClientCommands;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.mixin.accessors.ClientboundSetPlayerTeamPacketAccessor;
import com.wynntils.mc.utils.McUtils;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow private CommandDispatcher<SharedSuggestionProvider> commands;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(
            Minecraft minecraft,
            Screen screen,
            Connection connection,
            GameProfile gameProfile,
            CallbackInfo ci) {
        commands = ClientCommands.loadCommands(commands);
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    public void onOnCommandTree(ClientboundCommandsPacket packet, CallbackInfo ci) {
        commands = ClientCommands.loadCommands(commands);
    }

    @Inject(
            method =
                    "handlePlayerInfo(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoPacket;)V",
            at = @At("RETURN"))
    private void handlePlayerInfoPost(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
        EventFactory.onPlayerInfoPacket(packet);
    }

    @Inject(
            method =
                    "handleTabListCustomisation(Lnet/minecraft/network/protocol/game/ClientboundTabListPacket;)V",
            at = @At("RETURN"))
    private void handleTabListCustomisationPost(ClientboundTabListPacket packet, CallbackInfo ci) {
        EventFactory.onTabListCustomisation(packet);
    }

    @Inject(
            method =
                    "handleResourcePack(Lnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
            at = @At("RETURN"))
    private void handleResourcePackPost(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        EventFactory.onResourcePack(packet);
    }

    @Inject(
            method =
                    "handleMovePlayer(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("RETURN"))
    private void handleMovePlayerPost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        EventFactory.onPlayerMove(packet);
    }

    @Inject(
            method =
                    "handleOpenScreen(Lnet/minecraft/network/protocol/game/ClientboundOpenScreenPacket;)V",
            at = @At("RETURN"))
    private void handleOpenScreenPost(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        EventFactory.onOpenScreen(packet);
    }

    @Inject(
            method =
                    "handleContainerClose(Lnet/minecraft/network/protocol/game/ClientboundContainerClosePacket;)V",
            at = @At("RETURN"))
    private void handleContainerClosePost(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        EventFactory.onContainerClose(packet);
    }

    @Inject(
            method =
                    "handleSetPlayerTeamPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetPlayerTeamPacketPre(
            ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (((ClientboundSetPlayerTeamPacketAccessor) packet).getMethod() != 0
                && McUtils.mc().level.getScoreboard().getPlayerTeam(packet.getName()) == null) {
            ci.cancel();
        }
    }
}
