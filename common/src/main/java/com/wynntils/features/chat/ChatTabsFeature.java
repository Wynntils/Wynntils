/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.google.common.collect.Sets;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.ChatScreenCreateEvent;
import com.wynntils.mc.event.ChatScreenSendEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.chattabs.ChatTabsScreen;
import com.wynntils.services.chat.type.ChatTab;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class ChatTabsFeature extends Feature {
    // This is kept here only for config persistence, the "real" list is in ChatTabService
    // and it must be updated whenever this config is updated.
    @Persisted
    public final HiddenConfig<List<ChatTab>> chatTabs = new HiddenConfig<>(new ArrayList<>(List.of(
            new ChatTab("All", false, null, null, null),
            new ChatTab("Global", false, null, Sets.newHashSet(RecipientType.GLOBAL), null),
            new ChatTab("Local", false, null, Sets.newHashSet(RecipientType.LOCAL), null),
            new ChatTab("Guild", false, "/g  ", Sets.newHashSet(RecipientType.GUILD), null),
            new ChatTab("Party", false, "/p  ", Sets.newHashSet(RecipientType.PARTY), null),
            new ChatTab("Private", false, "/r  ", Sets.newHashSet(RecipientType.PRIVATE), null),
            new ChatTab("Shout", false, null, Sets.newHashSet(RecipientType.SHOUT), null))));

    @Persisted
    private final Config<Boolean> oldTabHotkey = new Config<>(false);

    @SubscribeEvent
    public void onChatScreenCreate(ChatScreenCreateEvent event) {
        event.setScreen(new ChatTabsScreen(event.getDefaultText(), oldTabHotkey.get()));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            Services.ChatTab.disable();
            return;
        }

        if (!Services.ChatTab.isEnabled()) {
            Services.ChatTab.enable();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatScreenSend(ChatScreenSendEvent event) {
        Services.ChatTab.sendChat(event.getInput());
        event.setCanceled(true);
    }

    @Override
    public void onEnable() {
        Services.ChatTab.setChatTabs(chatTabs.get());
        Services.ChatTab.enable();

        Screen screen = McUtils.screen();
        if (screen instanceof ChatScreen chatScreen) {
            if (screen instanceof ChatTabsScreen) return;

            McUtils.setScreen(new ChatTabsScreen("", oldTabHotkey.get()));
        }
    }

    @Override
    public void onDisable() {
        Services.ChatTab.disable();
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Services.ChatTab.setChatTabs(chatTabs.get());
    }
}
