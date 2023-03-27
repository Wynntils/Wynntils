/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.LoadingProgressEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.characterselector.LoadingScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomLoadingScreenFeature extends Feature {
    private final LoadingScreen loadingScreen = LoadingScreen.create();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScreenOpenPre(ScreenOpenedEvent.Pre event) {
        if (event.getScreen() instanceof ProgressScreen) {
            event.setCanceled(true);
        }
        if (event.getScreen() instanceof ReceivingLevelScreen) {
            event.setCanceled(true);
            loadingScreen.setMessage("Receiving terrain...");
        }
    }

    @SubscribeEvent
    public void onLoadingProgress(LoadingProgressEvent event) {
        loadingScreen.setMessage(event.getMessage());
    }

    @SubscribeEvent
    public void onResourcePack(ResourcePackEvent e) {
        loadingScreen.setMessage("Downloading resource pack...");
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        switch (event.getNewState()) {
            case CONNECTING -> {
                loadingScreen.setMessage("Connecting...");
                McUtils.mc().setScreen(loadingScreen);
            }
            case INTERIM -> loadingScreen.setMessage("Joining Wynncraft world...");
            case WORLD -> McUtils.mc().setScreen(null);
        }
    }
}
