/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
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
import net.minecraft.network.chat.MutableComponent;
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

        if (event.getRoomTimes().size() != Models.Raid.ROOM_TIMERS_COUNT) {
            WynntilsMod.error("Unexpected room count on raid completion: "
                    + event.getRoomTimes().size());
            return;
        }

        MutableComponent raidComponents = Component.literal("");

        raidComponents.append(Component.literal(event.getRaid().getName())
                .withStyle(ChatFormatting.GOLD)
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        raidComponents.append(Component.literal("\n\n"));

        raidComponents.append(Component.literal("Challenge 1: ").withStyle(ChatFormatting.LIGHT_PURPLE));

        long challengeOneMinutes = (event.getRoomTimes().get(0) / 1000) / 60;
        long challengeOneSeconds = (event.getRoomTimes().get(0) / 1000) % 60;
        long challengeOneMilliseconds = event.getRoomTimes().get(0) % 1000;
        String challengeOneTime =
                String.format("%02d:%02d.%03d\n", challengeOneMinutes, challengeOneSeconds, challengeOneMilliseconds);

        raidComponents.append(Component.literal(challengeOneTime).withStyle(ChatFormatting.AQUA));

        raidComponents.append(Component.literal("Challenge 2: ").withStyle(ChatFormatting.LIGHT_PURPLE));

        long challengeTwoMinutes = (event.getRoomTimes().get(1) / 1000) / 60;
        long challengeTwoSeconds = (event.getRoomTimes().get(1) / 1000) % 60;
        long challengeTwoMilliseconds = event.getRoomTimes().get(1) % 1000;
        String challengeTwoTime =
                String.format("%02d:%02d.%03d\n", challengeTwoMinutes, challengeTwoSeconds, challengeTwoMilliseconds);

        raidComponents.append(Component.literal(challengeTwoTime).withStyle(ChatFormatting.AQUA));

        raidComponents.append(Component.literal("Challenge 3: ").withStyle(ChatFormatting.LIGHT_PURPLE));

        long challengeThreeMinutes = (event.getRoomTimes().get(2) / 1000) / 60;
        long challengeThreeSeconds = (event.getRoomTimes().get(2) / 1000) % 60;
        long challengeThreeMilliseconds = event.getRoomTimes().get(2) % 1000;
        String challengeThreeTime = String.format(
                "%02d:%02d.%03d\n\n", challengeThreeMinutes, challengeThreeSeconds, challengeThreeMilliseconds);

        raidComponents.append(Component.literal(challengeThreeTime).withStyle(ChatFormatting.AQUA));

        raidComponents.append(Component.literal("Boss: ").withStyle(ChatFormatting.DARK_RED));

        long bossMinutes = (event.getRoomTimes().get(3) / 1000) / 60;
        long bossSeconds = (event.getRoomTimes().get(3) / 1000) % 60;
        long bossMilliseconds = event.getRoomTimes().get(3) % 1000;
        String bossTime = String.format("%02d:%02d.%03d\n\n", bossMinutes, bossSeconds, bossMilliseconds);

        raidComponents.append(Component.literal(bossTime).withStyle(ChatFormatting.AQUA));

        raidComponents.append(Component.literal("Intermission: ").withStyle(ChatFormatting.DARK_GRAY));

        long intermissionMinutes = (event.getRoomTimes().get(4) / 1000) / 60;
        long intermissionSeconds = (event.getRoomTimes().get(4) / 1000) % 60;
        long intermissionMilliseconds = event.getRoomTimes().get(4) % 1000;
        String intermissionTime =
                String.format("%02d:%02d.%03d\n", intermissionMinutes, intermissionSeconds, intermissionMilliseconds);

        raidComponents.append(Component.literal(intermissionTime).withStyle(ChatFormatting.AQUA));

        raidComponents.append(Component.literal("Total: ").withStyle(ChatFormatting.DARK_PURPLE));

        long totalMinutes = (event.getRaidTime() / 1000) / 60;
        long totalSeconds = (event.getRaidTime() / 1000) % 60;
        long totalMilliseconds = event.getRaidTime() % 1000;
        String totalTime = String.format("%02d:%02d.%03d", totalMinutes, totalSeconds, totalMilliseconds);

        raidComponents.append(Component.literal(totalTime).withStyle(ChatFormatting.AQUA));

        McUtils.sendMessageToClient(raidComponents);
    }

    @SubscribeEvent
    public void onRaidPersonalBest(RaidNewBestTimeEvent event) {
        if (!printTimes.get()) return;

        long minutes = (event.getTime() / 1000) / 60;
        long seconds = (event.getTime() / 1000) % 60;
        long milliseconds = event.getTime() % 1000;

        McUtils.sendMessageToClient(Component.translatable(
                        "feature.wynntils.raidProgress.personalBest",
                        event.getRaid().getName(),
                        minutes,
                        seconds,
                        milliseconds)
                .withStyle(ChatFormatting.GOLD)
                .withStyle(ChatFormatting.BOLD));

        if (playSoundOnBest.get()) {
            McUtils.playSoundAmbient(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);
        }
    }
}
