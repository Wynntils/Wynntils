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
import org.jetbrains.annotations.NotNull;
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
        Map<UUID, LerpingBossEvent> _events = events;

        packet.dispatch(new ClientboundBossEventPacket.Handler() {
            public void add(
                    @NotNull UUID id,
                    @NotNull Component name,
                    float progress,
                    BossEvent.@NotNull BossBarColor color,
                    BossEvent.@NotNull BossBarOverlay overlay,
                    boolean darkenScreen,
                    boolean playMusic,
                    boolean createWorldFog) {
                _events.put(
                        id,
                        new LerpingBossEvent(
                                id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog));
            }

            public void remove(@NotNull UUID id) {
                _events.remove(id);
            }

            public void updateProgress(@NotNull UUID id, float progress) {
                if (!_events.containsKey(id)) return;
                (_events.get(id)).setProgress(progress);
            }

            public void updateName(@NotNull UUID id, @NotNull Component name) {
                if (!_events.containsKey(id)) return;
                (_events.get(id)).setName(name);
            }

            public void updateStyle(
                    @NotNull UUID id,
                    BossEvent.@NotNull BossBarColor color,
                    BossEvent.@NotNull BossBarOverlay overlay) {
                if (!_events.containsKey(id)) return;
                LerpingBossEvent lerpingBossEvent = _events.get(id);
                lerpingBossEvent.setColor(color);
                lerpingBossEvent.setOverlay(overlay);
            }

            public void updateProperties(
                    @NotNull UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                if (!_events.containsKey(id)) return;
                LerpingBossEvent lerpingBossEvent = _events.get(id);
                lerpingBossEvent.setDarkenScreen(darkenScreen);
                lerpingBossEvent.setPlayBossMusic(playMusic);
                lerpingBossEvent.setCreateWorldFog(createWorldFog);
            }
        });
    }
}
