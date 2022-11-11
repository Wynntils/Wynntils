/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.core.chat.tabs.ChatTab;
import com.wynntils.core.chat.tabs.ChatTabModel;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderChatEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatTabsFeature extends UserFeature {
    public static ChatTabsFeature INSTANCE;

    @Config(visible = false)
    public List<ChatTab> chatTabs = Arrays.asList(
            new ChatTab("All", true, Sets.newHashSet(RecipientType.values()), null),
            new ChatTab("Global", true, Sets.newHashSet(RecipientType.GLOBAL), null),
            new ChatTab("Local", true, Sets.newHashSet(RecipientType.LOCAL), null),
            new ChatTab("Guild", true, Sets.newHashSet(RecipientType.GUILD), null),
            new ChatTab("Party", true, Sets.newHashSet(RecipientType.PARTY), null),
            new ChatTab("Private", true, Sets.newHashSet(RecipientType.PRIVATE), null),
            new ChatTab("Shout", true, Sets.newHashSet(RecipientType.SHOUT), null));

    @TypeOverride
    public Type chatTabsType = new TypeToken<List<ChatTab>>() {}.getType();

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ChatModel.class, ChatTabModel.class);
    }

    // We do this here, and not in ChatTabModel to not introduce a feature-model dependency.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatMessageReceivedEvent event) {
        // Firstly, find the FIRST matching tab with high priority
        for (ChatTab chatTab : chatTabs) {
            if (chatTab.isLowPriority()) continue;

            if (chatTab.matchMessageFromEvent(event)) {
                ChatTabModel.addMessageToTab(chatTab, event.getMessage());
                return;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : chatTabs) {
            if (!chatTab.isLowPriority()) continue;

            if (chatTab.matchMessageFromEvent(event)) {
                ChatTabModel.addMessageToTab(chatTab, event.getMessage());
            }
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            // TODO: Inject chat tab buttons
        }
    }

    @SubscribeEvent
    public void onChatRender(RenderChatEvent event) {
        // TODO: Inject focused chat tab here
    }
}
