/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.raid.event.RaidEndedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.overlays.RaidProgressOverlay;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class RaidProgressFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final RaidProgressOverlay raidProgressOverlay = new RaidProgressOverlay();

    @Persisted
    public final Config<Boolean> printTimes = new Config<>(true);

    @Persisted
    public final Config<Boolean> playSoundOnBest = new Config<>(true);

    @SubscribeEvent
    public void onRaidCompleted(RaidEndedEvent.Completed event) {
        if (!printTimes.get()) return;

        int minutes = event.getRaidTime() / 60;
        int seconds = event.getRaidTime() % 60;

        McUtils.sendMessageToClient(Component.translatable(
                        "feature.wynntils.raidProgress.completedRaid",
                        event.getRaid().getName(),
                        minutes,
                        seconds)
                .withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public void onRaidPersonalBest(RaidNewBestTimeEvent event) {
        if (!printTimes.get()) return;

        int minutes = event.getTime() / 60;
        int seconds = event.getTime() % 60;

        McUtils.sendMessageToClient(Component.translatable(
                        "feature.wynntils.raidProgress.personalBest",
                        event.getRaid().getName(),
                        minutes,
                        seconds)
                .withStyle(ChatFormatting.GOLD));

        if (playSoundOnBest.get()) {
            McUtils.playSoundAmbient(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);
        }
    }
}
