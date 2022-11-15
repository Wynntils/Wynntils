/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatTabModel extends Model {
    private static final ChatComponent EMPTY_CHAT_COMPONENT = new ChatComponent(McUtils.mc());

    private static ChatTab focusedTab = null;

    private static final Map<ChatTab, ChatComponent> chatTabData = new ConcurrentHashMap<>();

    public static void init() {}

    public static void disable() {
        chatTabData.clear();
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.NOT_CONNECTED) {
            chatTabData.clear();
            return;
        }

        if (event.getNewState() != WorldStateManager.State.WORLD) {
            setFocusedTab(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChatPacket(ChatPacketReceivedEvent event) {
        if (event.getType() != ChatType.CHAT) return;
        if (focusedTab == null) return;

        // Cancel all remaining messages, if we have a focused tab, we will handle it.
        event.setCanceled(true);
    }

    public static void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        chatTabData.get(tab).addMessage(message);
    }

    public static void setFocusedTab(ChatTab focused) {
        focusedTab = focused;

        if (focusedTab == null) {
            McUtils.mc().gui.chat = EMPTY_CHAT_COMPONENT;
        } else {
            chatTabData.putIfAbsent(focusedTab, new ChatComponent(McUtils.mc()));
            McUtils.mc().gui.chat = chatTabData.get(focusedTab);
        }
    }

    public static ChatTab getFocusedTab() {
        return focusedTab;
    }
}
