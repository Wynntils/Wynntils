/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.UpdateManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UpdatesFeature extends UserFeature {
    @Config
    public boolean updateReminder = true;

    @Config
    public boolean autoUpdate = false;

    private boolean firstJoin = true;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.NOT_CONNECTED) {
            firstJoin = true;
            return;
        }

        if (event.getNewState() != WorldStateManager.State.WORLD || !firstJoin) return;

        firstJoin = false;

        CompletableFuture.runAsync(() -> {
            UpdateManager.getLatestBuild().whenCompleteAsync((version, throwable) -> {
                if (version == null) {
                    WynntilsMod.info("Couldn't fetch latest version, not attempting update reminder or auto-update.");
                    return;
                }

                if (Objects.equals(version, WynntilsMod.getVersion())) {
                    WynntilsMod.info("Mod is on latest version, not attempting update reminder or auto-update.");
                    return;
                }

                if (updateReminder) {
                    remindToUpdateIfExists(version);
                }

                if (autoUpdate) {
                    if (WynntilsMod.isDevelopmentEnvironment()) {
                        WynntilsMod.info("Tried to auto-update, but we are in development environment.");
                        return;
                    }

                    WynntilsMod.info("Attempting to auto-update.");

                    McUtils.sendMessageToClient(new TextComponent("Auto-updating...").withStyle(ChatFormatting.YELLOW));

                    CompletableFuture<UpdateManager.UpdateResult> completableFuture = UpdateManager.tryUpdate();

                    completableFuture.whenCompleteAsync((result, t) -> {
                        switch (result) {
                            case SUCCESSFUL -> McUtils.sendMessageToClient(new TextComponent(
                                            "Successfully downloaded Wynntils/Artemis update. It will apply on shutdown.")
                                    .withStyle(ChatFormatting.DARK_GREEN));
                            case ERROR -> McUtils.sendMessageToClient(
                                    new TextComponent("Error applying Wynntils/Artemis update.")
                                            .withStyle(ChatFormatting.DARK_RED));
                            case ALREADY_ON_LATEST -> McUtils.sendMessageToClient(
                                    new TextComponent("Wynntils/Artemis is already on latest version.")
                                            .withStyle(ChatFormatting.YELLOW));
                        }
                    });
                }
            });
        });
    }

    private static void remindToUpdateIfExists(String newVersion) {
        TextComponent clickable = new TextComponent("here.");
        clickable.setStyle(clickable
                .getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/update"))
                .withUnderlined(true)
                .withBold(true));
        McUtils.sendMessageToClient(new TextComponent("[Wynntils/Artemis]: Version " + newVersion
                        + " is the latest version, but you are using build "
                        + WynntilsMod.getVersion() + ". Please consider updating by clicking ")
                .append(clickable)
                .append(new TextComponent(
                        "\nPlease note that Artemis is in alpha, and newer builds might introduce bugs."))
                .withStyle(ChatFormatting.GREEN));
    }
}
