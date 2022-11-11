/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.managers.Model;
import com.wynntils.mc.utils.McUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public class ChatTabModel extends Model {
    private static Map<ChatTab, ChatComponent> chatTabData = Map.of();

    public static void init() {
        chatTabData = new ConcurrentHashMap<>();
    }

    public static void disable() {
        chatTabData.clear();
    }

    public static void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        chatTabData.get(tab).addMessage(message);
    }
}
