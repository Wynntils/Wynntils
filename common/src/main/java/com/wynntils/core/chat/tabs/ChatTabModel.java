/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderChatEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatTabModel extends Model {
    private static final ChatComponent EMPTY_CHAT_COMPONENT = new ChatComponent(McUtils.mc());

    private static ChatTab focusedTab = null;

    private static Map<ChatTab, ChatComponent> chatTabData = Map.of();

    public static void init() {
        chatTabData = new ConcurrentHashMap<>();
    }

    public static void disable() {
        chatTabData.clear();
    }

    @SubscribeEvent
    public static void onChatRender(RenderChatEvent event) {
        if (focusedTab == null) return;

        // EMPTY_CHAT_COMPONENT is a dummy component that is used to render an empty chat, as chatTabData instance is
        // only created when there is a message.
        event.setRenderedChat(chatTabData.getOrDefault(focusedTab, EMPTY_CHAT_COMPONENT));
    }

    public static void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        chatTabData.get(tab).addMessage(message);
    }

    public static void setFocusedTab(ChatTab focused) {
        focusedTab = focused;
    }

    public static ChatTab getFocusedTab() {
        return focusedTab;
    }
}
