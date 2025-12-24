/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.raid.event.RaidEndedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.overlays.RaidProgressOverlay;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class RaidProgressFeature extends Feature {
    private static final String TIME_FORMAT_MILLISECONDS = "%02d:%02d.%03d";
    private static final String TIME_FORMAT_SECONDS = "%02d:%02d";

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final RaidProgressOverlay raidProgressOverlay = new RaidProgressOverlay();

    @Persisted
    private final Config<Boolean> printTimes = new Config<>(true);

    @Persisted
    private final Config<Boolean> playSoundOnBest = new Config<>(true);

    public RaidProgressFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onRaidCompleted(RaidEndedEvent.Completed event) {
        if (!printTimes.get()) return;

        if (event.getRaid().completedChallengeCount() == 0) {
            WynntilsMod.error("Completed raid but no completed rooms were tracked");
            return;
        }

        MutableComponent raidComponents = Component.literal("");

        raidComponents.append(Component.literal(event.getRaid().getRaidKind().getRaidName())
                .withStyle(ChatFormatting.GOLD)
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        raidComponents.append(Component.literal("\n\n"));

        for (int i = 0; i < event.getRaid().getRaidKind().getChallengeCount(); i++) {
            if (event.getRaid().getRoomByNumber(i + 1) == null) {
                WynntilsMod.warn("Completed raid "
                        + event.getRaid().getRaidKind().getRaidName() + " but missing challenge room " + i);
                continue;
            }

            raidComponents.append(
                    Component.literal(event.getRaid().getRoomByNumber(i + 1).getRoomName() + ": ")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
            raidComponents.append(Component.literal(
                            formatTime(event.getRaid().getRoomByNumber(i + 1).getRoomTotalTime()))
                    .withStyle(ChatFormatting.AQUA));
            if (raidProgressOverlay.showDamage.get()) {
                raidComponents
                        .append(Component.literal(" (").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(StringUtils.integerToShortString(
                                        event.getRaid().getRoomByNumber(i + 1).getRoomDamage()))
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
            }
            raidComponents.append(Component.literal("\n"));
        }

        raidComponents.append(Component.literal("\n"));

        for (int i = 0; i < event.getRaid().getRaidKind().getBossCount(); i++) {
            int bossRoomNum = i + event.getRaid().getRaidKind().getChallengeCount() + 1;

            if (event.getRaid().getRoomByNumber(bossRoomNum) == null) {
                WynntilsMod.warn("Completed raid "
                        + event.getRaid().getRaidKind().getRaidName() + " but missing boss room " + bossRoomNum);
                continue;
            }

            String bossName = event.getRaid().getRoomByNumber(bossRoomNum).getRoomName();

            if (bossName.equals("The ##### Anomaly")) {
                raidComponents
                        .append(Component.literal("The ").withStyle(ChatFormatting.DARK_RED))
                        .append(Component.literal("#####")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.OBFUSCATED))
                        .append(Component.literal(" Anomaly: ").withStyle(ChatFormatting.DARK_RED));
            } else {
                raidComponents.append(Component.literal(bossName + ": ").withStyle(ChatFormatting.DARK_RED));
            }

            raidComponents.append(Component.literal(formatTime(
                            event.getRaid().getRoomByNumber(bossRoomNum).getRoomTotalTime()))
                    .withStyle(ChatFormatting.AQUA));
            if (raidProgressOverlay.showDamage.get()) {
                raidComponents
                        .append(Component.literal(" (").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(StringUtils.integerToShortString(event.getRaid()
                                        .getRoomByNumber(bossRoomNum)
                                        .getRoomDamage()))
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
            }
            raidComponents.append(Component.literal("\n"));
        }

        if (raidProgressOverlay.showIntermission.get()) {
            raidComponents.append(Component.literal("\nIntermission: ").withStyle(ChatFormatting.DARK_GRAY));
            raidComponents.append(Component.literal(formatTime(event.getRaid().getIntermissionTime()))
                    .withStyle(ChatFormatting.AQUA));
        }

        raidComponents.append(Component.literal("\nTotal: ").withStyle(ChatFormatting.DARK_PURPLE));
        long raidTime = event.getRaid().getTimeInRaid()
                - (raidProgressOverlay.totalIntermission.get()
                        ? 0
                        : event.getRaid().getIntermissionTime());
        raidComponents.append(Component.literal(formatTime(raidTime)).withStyle(ChatFormatting.AQUA));
        if (raidProgressOverlay.showDamage.get()) {
            raidComponents
                    .append(Component.literal(" (").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(StringUtils.integerToShortString(
                                    event.getRaid().getRaidDamage()))
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
        }

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
                        event.getRaidName(),
                        minutes,
                        seconds,
                        milliseconds)
                .withStyle(ChatFormatting.GOLD)
                .withStyle(ChatFormatting.BOLD));

        if (playSoundOnBest.get()) {
            McUtils.playSoundAmbient(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);
        }
    }

    private String formatTime(long time) {
        long minutes = (time / 1000) / 60;
        long seconds = (time / 1000) % 60;
        long milliseconds = time % 1000;

        if (raidProgressOverlay.showMilliseconds.get()) {
            return String.format(TIME_FORMAT_MILLISECONDS, minutes, seconds, milliseconds);
        } else {
            return String.format(TIME_FORMAT_SECONDS, minutes, seconds);
        }
    }
}
