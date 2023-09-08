/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.LoadingProgressEvent;
import com.wynntils.mc.event.LocalSoundEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.characterselector.LoadingScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomLoadingScreenFeature extends Feature {
    private LoadingScreen loadingScreen;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScreenOpenPre(ScreenOpenedEvent.Pre event) {
        if (loadingScreen == null) return;

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
        if (loadingScreen == null) {
            loadingScreen = LoadingScreen.create();
            loadingScreen.setMessage(event.getMessage());
            McUtils.mc().setScreen(loadingScreen);
        }

        loadingScreen.setMessage(event.getMessage());
    }

    @SubscribeEvent
    public void onResourcePack(ResourcePackEvent e) {
        if (loadingScreen == null) return;

        loadingScreen.setMessage("Downloading resource pack...");
    }

    @SubscribeEvent
    public void onTitleSetText(TitleSetTextEvent e) {
        if (loadingScreen == null) return;

        loadingScreen.setTitle(e.getComponent().getString());
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        if (loadingScreen == null) return;

        loadingScreen.setSubtitle(e.getComponent().getString());
    }

    @SubscribeEvent
    public void onPlayerSound(LocalSoundEvent event) {
        if (loadingScreen == null) return;

        // Silence all player sounds while loading (falling and equip sounds)
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        switch (event.getNewState()) {
            case CONNECTING -> {
                loadingScreen = LoadingScreen.create();
                loadingScreen.setMessage("Connecting...");
                McUtils.mc().setScreen(loadingScreen);
            }
            case INTERIM -> {
                if (loadingScreen == null) return;

                loadingScreen.setMessage("Joining Wynncraft world...");
            }
            case WORLD, HUB -> {
                if (loadingScreen == null) return;

                loadingScreen = null;
                McUtils.mc().setScreen(null);
            }
        }
    }
}
