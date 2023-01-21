/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.mixin.invokers.ChatScreenInvoker;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ChatTabModel extends Model {
    private ChatTab focusedTab = null;

    private final Map<ChatTab, ChatComponent> chatTabData = new ConcurrentHashMap<>();
    private final Map<ChatTab, Boolean> unreadMessages = new ConcurrentHashMap<>();

    @Override
    public void disable() {
        chatTabData.clear();
        unreadMessages.clear();
        setFocusedTab(null);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            chatTabData.clear();
            unreadMessages.clear();
            setFocusedTab(null);
        }
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;
        if (focusedTab == null
                || focusedTab.getAutoCommand() == null
                || focusedTab.getAutoCommand().isEmpty()) return;

        replaceChatText(chatScreen, focusedTab.getAutoCommand());
    }

    private void replaceChatText(ChatScreen chatScreen, String autoCommand) {
        ((ChatScreenInvoker) chatScreen).invokeInsertText(autoCommand, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatPacket(ChatPacketReceivedEvent.Player event) {
        // FIXME: I don't believe this ever happens?
        if (focusedTab == null) return;

        // Cancel all remaining messages, if we have a focused tab, we will handle it.
        event.setCanceled(true);
    }

    public void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        chatTabData.get(tab).addMessage(message);

        if (focusedTab != tab) {
            unreadMessages.put(tab, true);
        }
    }

    public void setFocusedTab(ChatTab focused) {
        if (Objects.equals(focusedTab, focused)) {
            // do not create new chat component if we are already focused on the tab
            return;
        }

        ChatTab oldFocused = focusedTab;

        focusedTab = focused;

        if (focusedTab == null) {
            McUtils.mc().gui.chat = new ChatComponent(McUtils.mc());
        } else {
            chatTabData.putIfAbsent(focusedTab, new ChatComponent(McUtils.mc()));
            unreadMessages.put(focusedTab, false);
            McUtils.mc().gui.chat = chatTabData.get(focusedTab);

            // If chat screen is open, and current message is empty or the previous auto command, set our auto command
            if (McUtils.mc().screen instanceof ChatScreen chatScreen
                    && (chatScreen.input.getValue().isEmpty()
                            || oldFocused == null
                            || chatScreen.input.getValue().equals(oldFocused.getAutoCommand()))) {
                String autoCommand = focusedTab.getAutoCommand() == null ? "" : focusedTab.getAutoCommand();
                replaceChatText(chatScreen, autoCommand);
            }
        }
    }

    public ChatTab getFocusedTab() {
        return focusedTab;
    }

    public boolean hasUnreadMessages(ChatTab tab) {
        return unreadMessages.getOrDefault(tab, false);
    }
}
