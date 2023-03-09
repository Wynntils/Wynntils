/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.components.Manager;
import com.wynntils.core.json.TypeOverride;
import com.wynntils.core.storage.Storage;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.mixin.invokers.ChatScreenInvoker;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ChatTabManager extends Manager {
    private ChatTab focusedTab = null;

    private Storage<List<ChatTab>> chatTabs = new Storage(new ArrayList<>(List.of(
            new ChatTab("All", false, null, null, null),
            new ChatTab("Global", false, null, Sets.newHashSet(RecipientType.GLOBAL), null),
            new ChatTab("Local", false, null, Sets.newHashSet(RecipientType.LOCAL), null),
            new ChatTab("Guild", false, "/g  ", Sets.newHashSet(RecipientType.GUILD), null),
            new ChatTab("Party", false, "/p  ", Sets.newHashSet(RecipientType.PARTY), null),
            new ChatTab("Private", false, "/r  ", Sets.newHashSet(RecipientType.PRIVATE), null),
            new ChatTab("Shout", false, null, Sets.newHashSet(RecipientType.SHOUT), null))));

    @TypeOverride
    private final Type chatTabsType = new TypeToken<ArrayList<ChatTab>>() {}.getType();

    private final Map<ChatTab, ChatComponent> chatTabData = new ConcurrentHashMap<>();
    private final Map<ChatTab, Boolean> unreadMessages = new ConcurrentHashMap<>();

    public ChatTabManager() {
        super(List.of());
    }

    public Stream<ChatTab> getTabs() {
        return chatTabs.get().stream();
    }

    public ChatTab getTab(int index) {
        return chatTabs.get().get(index);
    }

    public int getTabCount() {
        return chatTabs.get().size();
    }

    public boolean isTabListEmpty() {
        return getTabCount() == 0;
    }

    public void addTab(int insertIndex, ChatTab chatTab) {
        chatTabs.get().add(insertIndex, chatTab);
        chatTabs.touched();
    }

    public void removeTab(ChatTab chatTab) {
        chatTabs.get().remove(chatTab);
        chatTabs.touched();
    }

    public int getTabIndex(ChatTab edited) {
        return chatTabs.get().indexOf(edited);
    }

    public int getNextFocusedTab() {
        return (getTabIndex(getFocusedTab()) + 1) % getTabCount();
    }

    public void resetFocusedTab() {
        if (!isTabListEmpty()) {
            setFocusedTab(0);
        }
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
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
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

    public void setFocusedTab(int index) {
        setFocusedTab(getTab(index));
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

    public void matchMessage(ClientsideMessageEvent event) {
        // Firstly, find the FIRST matching tab with high priority
        for (ChatTab chatTab : chatTabs.get()) {
            if (!chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getComponent());
                return;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : chatTabs.get()) {
            if (chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getComponent());
            }
        }
    }

    public void matchMessage(ChatMessageReceivedEvent event) {
        // Firstly, find the FIRST matching tab with high priority
        for (ChatTab chatTab : chatTabs.get()) {
            if (!chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getMessage());
                return;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : chatTabs.get()) {
            if (chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getMessage());
            }
        }
    }

    private void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        chatTabData.get(tab).addMessage(message);

        if (focusedTab != tab) {
            unreadMessages.put(tab, true);
        }
    }

    private boolean matchMessageFromEvent(ChatTab chatTab, ChatMessageReceivedEvent event) {
        if (chatTab.getFilteredTypes() != null
                && !chatTab.getFilteredTypes().isEmpty()
                && !chatTab.getFilteredTypes().contains(event.getRecipientType())) {
            return false;
        }

        return chatTab.getCustomRegexString() == null
                || chatTab.getCustomRegexString().isBlank()
                || chatTab.getCustomRegex()
                        .matcher(event.getOriginalCodedMessage())
                        .matches();
    }

    private boolean matchMessageFromEvent(ChatTab chatTab, ClientsideMessageEvent event) {
        if (chatTab.getFilteredTypes() != null
                && !chatTab.getFilteredTypes().isEmpty()
                && !chatTab.getFilteredTypes().contains(RecipientType.CLIENTSIDE)) {
            return false;
        }

        if (chatTab.getCustomRegexString() == null) {
            return true;
        }

        return chatTab.getCustomRegex().matcher(event.getOriginalCodedMessage()).matches();
    }
}
