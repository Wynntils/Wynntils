/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.mc.event.RemovePlayerFromTeamEvent;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Handler;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FixPacketBugsFeature extends FeatureBase {

    private static final int METHOD_ADD = 0;

    public FixPacketBugsFeature() {
        setupEventListener();
    }

    public static void fixBossEventPackage(
            ClientboundBossEventPacket packet, Handler handler, Map<UUID, LerpingBossEvent> bossEvents) {
        packet.dispatch(new HandlerWrapper(handler, bossEvents));
    }

    @SubscribeEvent
    public void onSetPlayerTeamPacket(SetPlayerTeamEvent event) {
        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (event.getMethod() != METHOD_ADD
                && McUtils.mc().level.getScoreboard().getPlayerTeam(event.getTeamName()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRemovePlayerFromTeam(RemovePlayerFromTeamEvent event) {
        // Work around bug in Wynncraft that causes NPEs in Vanilla
        PlayerTeam playerTeamFromUserName = McUtils.mc().level.getScoreboard().getPlayersTeam(event.getUsername());
        if (playerTeamFromUserName != event.getPlayerTeam()) {
            event.setCanceled(true);
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
