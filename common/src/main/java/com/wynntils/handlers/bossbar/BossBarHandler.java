/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final Map<UUID, TrackedBar> presentBars = new HashMap<>();
    private final List<TrackedBar> knownBars = new ArrayList<>();

    public void registerBar(TrackedBar trackedBar) {
        knownBars.add(trackedBar);
    }

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

            for (TrackedBar bar : knownBars) {
                matcher = StyledText.fromComponent(name).getMatcher(bar.pattern);
                if (matcher.matches()) {
                    trackedBar = bar;
                    break;
                }
            }
            if (trackedBar == null) return;

            LerpingBossEvent bossEvent =
                    new LerpingBossEvent(id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog);
            trackedBar.setEvent(bossEvent);

            // Allow for others to try and cancel event
            BossBarAddedEvent barAddEvent = new BossBarAddedEvent(trackedBar);
            WynntilsMod.postEvent(barAddEvent);

            if (barAddEvent.isCanceled()) {
                trackedBar.setRendered(false);
                event.setCanceled(true);
            } else {
                trackedBar.setRendered(true);
            }

            trackedBar.onUpdateName(matcher);

            presentBars.put(id, trackedBar);
        }

        private void handleBarUpdate(UUID id, Consumer<TrackedBar> consumer) {
            TrackedBar trackedBar = presentBars.get(id);

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
                presentBars.remove(id);
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
                Matcher matcher = StyledText.fromComponent(name).getMatcher(trackedBar.pattern);
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
