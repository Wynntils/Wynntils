/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.McIf;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

    @Inject(
            method =
                    "handlePlayerInfo(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoPacket;)V",
            at = @At("RETURN"))
    public void handlePlayerInfoPost(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
        EventFactory.onPlayerInfoPacket(packet);
    }

    @Inject(
            method =
                    "handleTabListCustomisation(Lnet/minecraft/network/protocol/game/ClientboundTabListPacket;)V",
            at = @At("RETURN"))
    public void handleTabListCustomisationPost(ClientboundTabListPacket packet, CallbackInfo ci) {
        EventFactory.onTabListCustomisation(packet);
    }

    @Inject(
            method =
                    "handleResourcePack(Lnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
            at = @At("RETURN"))
    public void handleResourcePackPost(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        EventFactory.onResourcePack(packet);
    }

    @Inject(
            method =
                    "handleSetPlayerTeamPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void handleSetPlayerTeamPacketPre(
            ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (packet.getMethod() != 0
                && McIf.mc().level.getScoreboard().getPlayerTeam(packet.getName()) == null) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleAddPlayer(Lnet/minecraft/network/protocol/game/ClientboundAddPlayerPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void handleAddPlayerPre(ClientboundAddPlayerPacket packet, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes NPEs in Vanilla
        if (getPlayerInfo(packet.getPlayerId()) == null) {
            ci.cancel();
        }
    }
}
