/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Final
    @Shadow
    Map<UUID, LerpingBossEvent> events;

    @Redirect(
            method = "update",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket;dispatch(Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket$Handler;)V"))
    private void updatePre(ClientboundBossEventPacket packet, ClientboundBossEventPacket.Handler handler) {
        Map<UUID, LerpingBossEvent> bossEvents = events;

        packet.dispatch(new ClientboundBossEventPacket.Handler() {
            public void add(
                    UUID id,
                    Component name,
                    float progress,
                    BossEvent.BossBarColor color,
                    BossEvent.BossBarOverlay overlay,
                    boolean darkenScreen,
                    boolean playMusic,
                    boolean createWorldFog) {
                bossEvents.put(
                        id,
                        new LerpingBossEvent(
                                id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog));
            }

            public void remove(UUID id) {
                bossEvents.remove(id);
            }

            public void updateProgress(UUID id, float progress) {
                if (!bossEvents.containsKey(id)) return;
                (bossEvents.get(id)).setProgress(progress);
            }

            public void updateName(UUID id, Component name) {
                if (!bossEvents.containsKey(id)) return;
                (bossEvents.get(id)).setName(name);
            }

            public void updateStyle(UUID id, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
                if (!bossEvents.containsKey(id)) return;
                LerpingBossEvent lerpingBossEvent = bossEvents.get(id);
                lerpingBossEvent.setColor(color);
                lerpingBossEvent.setOverlay(overlay);
            }

            public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                if (!bossEvents.containsKey(id)) return;
                LerpingBossEvent lerpingBossEvent = bossEvents.get(id);
                lerpingBossEvent.setDarkenScreen(darkenScreen);
                lerpingBossEvent.setPlayBossMusic(playMusic);
                lerpingBossEvent.setCreateWorldFog(createWorldFog);
            }
        });
    }
}
