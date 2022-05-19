/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.mc.mixin.accessors.ClientboundSetPlayerTeamPacketAccessor;
import com.wynntils.mc.utils.McUtils;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Handler;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FixPacketBugsFeature extends FeatureBase {

    public FixPacketBugsFeature() {
        setupEventListener();
    }

    public static void fixBossEventPackage(
            ClientboundBossEventPacket packet, Handler handler, Map<UUID, LerpingBossEvent> bossEvents) {

        packet.dispatch(new HandlerWrapper(handler, bossEvents));
    }

    public static void fixSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (((ClientboundSetPlayerTeamPacketAccessor) packet).getMethod() != 0
                && McUtils.mc().level.getScoreboard().getPlayerTeam(packet.getName()) == null) {
            ci.cancel();
        }
    }

    public static void fixRemovePlayerFromTeam(
            PlayerTeam playerTeam, PlayerTeam playerTeamFromUserName, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes NPEs in Vanilla
        if (playerTeamFromUserName != playerTeam) {
            ci.cancel();
        }
    }

    private static class HandlerWrapper implements Handler {
        private final Handler wrappedHandler;
        private final Map<UUID, LerpingBossEvent> bossEvents;

        HandlerWrapper(Handler wrappedHandler, Map<UUID, LerpingBossEvent> bossEvents) {
            this.wrappedHandler = wrappedHandler;
            this.bossEvents = bossEvents;
        }

        @Override
        public void add(
                UUID id,
                Component name,
                float progress,
                BossBarColor color,
                BossBarOverlay overlay,
                boolean darkenScreen,
                boolean playMusic,
                boolean createWorldFog) {
            wrappedHandler.add(id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog);
        }

        @Override
        public void remove(UUID id) {
            wrappedHandler.remove(id);
        }

        @Override
        public void updateProgress(UUID id, float progress) {
            if (!bossEvents.containsKey(id)) return;

            wrappedHandler.updateProgress(id, progress);
        }

        @Override
        public void updateName(UUID id, Component name) {
            if (!bossEvents.containsKey(id)) return;

            wrappedHandler.updateName(id, name);
        }

        @Override
        public void updateStyle(UUID id, BossBarColor color, BossBarOverlay overlay) {
            if (!bossEvents.containsKey(id)) return;

            wrappedHandler.updateStyle(id, color, overlay);
        }

        @Override
        public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
            if (!bossEvents.containsKey(id)) return;

            wrappedHandler.updateProperties(id, darkenScreen, playMusic, createWorldFog);
        }
    }
}
