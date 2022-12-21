/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Handler;
import com.wynntils.core.managers.Managers;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.CustomBarAddEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.bossbar.BossBarModel;
import com.wynntils.wynn.objects.ClassType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BossBarHandler extends Handler {
    private final Map<UUID, TrackedBar> trackedBarsMap = new HashMap<>();

    // FixPacketBugsFeature gets in the way if receiveCanceled is not set
    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();

        packet.dispatch(new TrackedBarHandler(event));
    }

    private final class TrackedBarHandler implements ClientboundBossEventPacket.Handler {
        private final BossHealthUpdateEvent event;

        private TrackedBarHandler(BossHealthUpdateEvent event) {
            this.event = event;
        }

        @Override
        public void add(
                UUID id,
                Component name,
                float progress,
                BossEvent.BossBarColor color,
                BossEvent.BossBarOverlay overlay,
                boolean darkenScreen,
                boolean playMusic,
                boolean createWorldFog) {
            TrackedBar trackedBar = null;
            Matcher matcher = null;

            ClassType userClass = Managers.Character.getCharacterInfo().getClassType();

            for (TrackedBar potentialTrackedBar : BossBarModel.KNOWN_BARS) {
                if (potentialTrackedBar.classType != userClass) continue;

                matcher = potentialTrackedBar.pattern.matcher(ComponentUtils.getCoded(name));
                if (matcher.matches()) {
                    trackedBar = potentialTrackedBar;
                    break;
                }
            }

            if (trackedBar == null) return;

            event.setCanceled(true);

            LerpingBossEvent bossEvent =
                    new LerpingBossEvent(id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog);
            trackedBar.setEvent(bossEvent);

            // Allow for others to try and cancel event
            CustomBarAddEvent barAddEvent = new CustomBarAddEvent(trackedBar.type);
            WynntilsMod.postEvent(barAddEvent);

            if (barAddEvent.isCanceled()) {
                trackedBar.setRendered(false);
            } else {
                trackedBar.setRendered(true);
                McUtils.mc().gui.getBossOverlay().events.put(id, bossEvent);
            }

            trackedBar.onUpdateName(matcher);

            trackedBarsMap.put(id, trackedBar);
        }

        private void handleBarUpdate(UUID id, Consumer<TrackedBar> consumer) {
            TrackedBar trackedBar = trackedBarsMap.get(id);

            if (trackedBar != null) {
                if (!trackedBar.isRendered()) {
                    event.setCanceled(true);
                }

                consumer.accept(trackedBar);
            }
        }

        @Override
        public void remove(UUID id) {
            handleBarUpdate(id, trackedBar -> {
                trackedBar.reset();
                trackedBarsMap.remove(id);
            });
        }

        @Override
        public void updateProgress(UUID id, float progress) {
            handleBarUpdate(id, trackedBar -> {
                event.setCanceled(true);
                trackedBar.getEvent().setProgress(progress);
                trackedBar.onUpdateProgress(progress);
            });
        }

        @Override
        public void updateName(UUID id, Component name) {
            handleBarUpdate(id, trackedBar -> {
                Matcher matcher = trackedBar.pattern.matcher(ComponentUtils.getCoded(name));
                if (!matcher.matches()) {
                    WynntilsMod.error("Failed to match already matched boss bar");
                    return;
                }

                trackedBar.onUpdateName(matcher);
            });
        }

        // We need to cancel the event even though we don't process it here
        @Override
        public void updateStyle(UUID id, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
            handleBarUpdate(id, trackedBar -> {});
        }

        @Override
        public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
            handleBarUpdate(id, trackedBar -> {});
        }
    }
}
