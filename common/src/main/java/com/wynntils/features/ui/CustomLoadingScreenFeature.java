/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.LoadingProgressEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TickAlwaysEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.loading.LoadingScreen;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomLoadingScreenFeature extends Feature {
    private static final String IGNORED_TITLE = "\uE000\uE001\uE000";
    private static final Pattern SERVER_SWITCH_PATTERN =
            Pattern.compile("§7Saving your player data before switching to §f(.*)§7...");

    private LoadingScreen loadingScreen;
    private Screen replacedScreen;
    private TickSchedulerManager.ScheduledTask delayedRemoval;
    private boolean allowClosing;

    public CustomLoadingScreenFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onTickAlways(TickAlwaysEvent e) {
        // Minecraft does connection logic work every tick, propagate tick to keep this behaviour
        if (replacedScreen != null) {
            replacedScreen.tick();
        }
    }

    @SubscribeEvent
    public void onChatMessageReceived(ChatMessageEvent.Match e) {
        if (e.getMessage().matches(SERVER_SWITCH_PATTERN)) {
            createCustomScreen();
            loadingScreen.setMessage(I18n.get("feature.wynntils.customLoadingScreen.switchingServer"));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onScreenClosed(ScreenClosedEvent.Pre event) {
        if (allowClosing) return;

        // Don't allow screens to close when they are "faked"
        if (isCustomScreenVisible()) {
            event.setCanceled(true);
        }
        // But update our knowledge of what screen Minecraft thinks it is showing
        if (replacedScreen != null) {
            replacedScreen.removed();
            replacedScreen = null;
        }

        if (McUtils.mc().level == null) {
            // Vanilla will interpret this as it should show the title menu, so cancel it
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onScreenOpened(ScreenOpenedEvent.Pre event) {
        Screen screen = event.getScreen();

        if (screen instanceof LoadingScreen) return;

        if (replacedScreen != null) {
            replacedScreen.removed();
            replacedScreen = null;
        }

        String messageUpdate = null;
        if (screen instanceof ProgressScreen ps) {
            messageUpdate = "feature.wynntils.customLoadingScreen.working";
        }
        if (screen instanceof ConnectScreen cs) {
            if (cs.status.getContents() instanceof TranslatableContents tc
                    && tc.getKey().equals("connect.transferring")) {
                messageUpdate = "feature.wynntils.customLoadingScreen.transferConnecting";
            }
        }
        if (screen instanceof DisconnectedScreen ds) {
            if (ds.details.reason().getContents() instanceof TranslatableContents tc
                    && tc.getKey().equals("disconnect.transfer")) {
                messageUpdate = "feature.wynntils.customLoadingScreen.transferRequest";
            }
        }
        if (screen instanceof ReceivingLevelScreen) {
            messageUpdate = "feature.wynntils.customLoadingScreen.receivingTerrain";
        }

        if (!isCustomScreenVisible()) {
            if (!Managers.Connection.onServer()) return;
            if (messageUpdate == null) return;

            // If we get one of our special screens during gameplay, show our custom loading screen
            createCustomScreen();
        }

        // We have a custom loading screen showing, maybe update it?
        if (messageUpdate != null) {
            loadingScreen.setMessage(I18n.get(messageUpdate));
        }

        // Make the screen think it is showing, but really don't let it show
        replacedScreen = screen;
        screen.init(McUtils.mc(), 1, 1);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onLoadingProgress(LoadingProgressEvent event) {
        if (!isCustomScreenVisible()) return;

        loadingScreen.setMessage(event.getMessage());
    }

    @SubscribeEvent
    public void onResourcePack(ServerResourcePackEvent.Load e) {
        if (!isCustomScreenVisible()) return;

        loadingScreen.setMessage(I18n.get("feature.wynntils.customLoadingScreen.resourcePack"));
    }

    @SubscribeEvent
    public void onTitleSetText(TitleSetTextEvent e) {
        if (!isCustomScreenVisible()) return;

        if (e.getComponent().getString().equals(IGNORED_TITLE)) return;

        loadingScreen.setStageTitle(e.getComponent().getString());
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        if (!isCustomScreenVisible()) return;

        loadingScreen.setSubtitle(e.getComponent().getString());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        switch (event.getNewState()) {
            case CONNECTING -> {
                createCustomScreen();
                loadingScreen.setMessage(I18n.get("feature.wynntils.customLoadingScreen.connecting"));
            }
            case INTERIM -> {
                if (!isCustomScreenVisible()) {
                    createCustomScreen();
                }

                // The HUB was just temporary
                cancelDelayedRemoval();

                loadingScreen.setMessage(I18n.get("feature.wynntils.customLoadingScreen.joiningWorld"));
            }
            case HUB -> {
                if (!isCustomScreenVisible()) return;

                // Unless connecting to lobby.wynncraft.com (or some odd situation occurs),
                // the hub will just flash by. Don't remove our custom loading screen for that.
                delayedRemoval = Managers.TickScheduler.scheduleLater(this::removeCustomScreen, 20);
            }
            case WORLD, NOT_CONNECTED, CHARACTER_SELECTION -> {
                if (!isCustomScreenVisible()) return;

                // We might have a delayed removal from HUB, remove it first
                cancelDelayedRemoval();

                // Give some time for the world to fully load to avoid flickering
                // before removing our custom loading screen
                delayedRemoval = Managers.TickScheduler.scheduleLater(this::removeCustomScreen, 20);
            }
        }
    }

    private boolean isCustomScreenVisible() {
        return loadingScreen != null;
    }

    private void createCustomScreen() {
        loadingScreen = LoadingScreen.create(this::onLoadingScreenClosed);
        allowClosing = false;
        McUtils.setScreen(loadingScreen);
    }

    private void removeCustomScreen() {
        delayedRemoval = null;
        loadingScreen = null;
        if (McUtils.screen() == null) {
            WynntilsMod.error("The custom LoadingScreen has disappeared");
        } else {
            McUtils.setScreen(null);
        }
    }

    private void onLoadingScreenClosed() {
        allowClosing = true;
        if (this.replacedScreen != null) {
            replacedScreen.onClose();
        }
        delayedRemoval = null;
        loadingScreen = null;
    }

    private void cancelDelayedRemoval() {
        if (delayedRemoval != null) {
            Managers.TickScheduler.cancel(delayedRemoval);
            delayedRemoval = null;
        }
    }
}
