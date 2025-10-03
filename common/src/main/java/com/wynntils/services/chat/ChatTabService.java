/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.chat.ChatTabsFeature;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.chat.type.ChatTab;
import com.wynntils.services.chat.type.ChatTabData;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public final class ChatTabService extends Service {
    private ChatTab focusedTab = null;

    private ChatComponent vanillaChatComponent;
    private WrappingChatComponent wrappingChatComponent;

    private final Map<ChatTab, ChatTabData> tabDataMap = new HashMap<>();
    // This is a copy of the config in ChatTabsFeature, stored there for persistence.
    private final List<ChatTab> chatTabs = new ArrayList<>();

    public ChatTabService() {
        super(List.of());
    }

    // region Chat Tab list and index
    public List<ChatTab> getChatTabs() {
        return chatTabs;
    }

    public ChatTab getTab(int index) {
        return getChatTabs().get(index);
    }

    public int getTabCount() {
        return getChatTabs().size();
    }

    public int getTabIndex(ChatTab edited) {
        return getChatTabs().indexOf(edited);
    }

    public int getTabIndexAfterFocused() {
        return (getTabIndex(getFocusedTab()) + 1) % getTabCount();
    }

    public int getTabIndexBeforeFocused() {
        int tabIndex = getTabIndex(getFocusedTab());
        return (tabIndex - 1 + getTabCount()) % getTabCount();
    }

    public void addTab(int insertIndex, ChatTab chatTab) {
        getChatTabs().add(insertIndex, chatTab);
        Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs.touched();

        tabDataMap.put(chatTab, new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString()));
    }

    public void removeTab(ChatTab chatTab) {
        getChatTabs().remove(chatTab);
        Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs.touched();

        tabDataMap.remove(chatTab);
    }

    public ChatComponent getChatComponent(ChatTab tab) {
        return tabDataMap.get(tab).getChatComponent();
    }

    public boolean hasUnreadMessages(ChatTab tab) {
        return tabDataMap.get(tab).hasUnreadMessages();
    }

    // endregion

    // region Focused Tab

    public ChatTab getFocusedTab() {
        return focusedTab;
    }

    public void setFocusedTab(int index) {
        setFocusedTab(getTab(index));
    }

    public void setFocusedTab(ChatTab focused) {
        focusedTab = focused;

        tabDataMap.get(focused).setUnreadMessages(false);
        wrappingChatComponent.setCurrentChatComponent(getChatComponent(focused));
    }
    // endregion

    // region Enable/disable

    public void enable() {
        if (isEnabled()) return;

        vanillaChatComponent = McUtils.mc().gui.chat;
        wrappingChatComponent = new WrappingChatComponent(McUtils.mc());
        McUtils.mc().gui.chat = wrappingChatComponent;

        reset();
        // Create a new ChatTabData for each tab
        getChatTabs()
                .forEach(chatTab -> tabDataMap.put(
                        chatTab, new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString())));

        // Restore all messages sent to the vanilla chat component to all the new tabs
        vanillaChatComponent.allMessages.reversed().forEach(msg -> {
            Component component = msg.content();
            StyledText styledText = StyledText.fromComponent(component);
            RecipientType recipientType = Handlers.Chat.getRecipientType(styledText, MessageType.FOREGROUND);
            List<ChatTab> recipientTabs = Services.ChatTab.getRecipientTabs(recipientType, styledText);

            recipientTabs.forEach(tab -> Services.ChatTab.getChatComponent(tab).addMessage(component));
        });

        if (getTabCount() != 0) {
            setFocusedTab(0);
        }
    }

    public void disable() {
        reset();
        focusedTab = null;

        McUtils.mc().gui.chat = vanillaChatComponent;
    }

    public boolean isEnabled() {
        return focusedTab != null;
    }
    // endregion

    // region Implementation details, support for WrappingChatComponent
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            reset();
        }
    }

    private void reset() {
        tabDataMap.clear();
    }

    public void forEachChatComponent(Consumer<ChatComponent> chatComponentConsumer) {
        tabDataMap.values().stream().map(ChatTabData::getChatComponent).forEach(chatComponentConsumer);
    }

    void markAsNewMessages(ChatTab tab) {
        if (tab != focusedTab) {
            tabDataMap.get(tab).setUnreadMessages(true);
        }
    }

    List<ChatTab> getRecipientTabs(RecipientType recipientType, StyledText styledText) {
        List<ChatTab> recipientTabs = new ArrayList<>();

        // Firstly, find the FIRST matching tab with high priority
        for (ChatTab chatTab : getChatTabs()) {
            if (!chatTab.consuming()) continue;

            if (matchMessage(chatTab, recipientType, styledText)) {
                recipientTabs.add(chatTab);
                return recipientTabs;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : getChatTabs()) {
            if (chatTab.consuming()) continue;

            if (matchMessage(chatTab, recipientType, styledText)) {
                recipientTabs.add(chatTab);
            }
        }
        return recipientTabs;
    }

    private boolean matchMessage(ChatTab chatTab, RecipientType recipientType, StyledText originalStyledText) {
        if (chatTab.filteredTypes() != null) {
            if (!chatTab.filteredTypes().isEmpty()) {
                if (!chatTab.filteredTypes().contains(recipientType)) {
                    return false;
                }
            }
        }

        Optional<Pattern> regex = tabDataMap.get(chatTab).getCustomRegex();
        if (regex.isEmpty()) return true;

        return originalStyledText.matches(regex.get());
    }
    // endregion

    /**
     * Sends a chat message respecting chat tab autocommand settings.
     * If no chat tab is actively selected, the message will be sent normally.
     * Since autocommands are still commands, they will be queued just like any other command.
     * They are also subject to the same ratelimits on Wynncraft.
     * @param message The message to send.
     */
    public void sendChat(String message) {
        if (message.isBlank()) return;

        if (!isEnabled()) {
            McUtils.sendChat(message);
            return;
        }

        String autoCommand = getFocusedTab().autoCommand();
        if (autoCommand != null && !autoCommand.isBlank()) {
            autoCommand = autoCommand.startsWith("/") ? autoCommand.substring(1) : autoCommand;
            Handlers.Command.sendCommandImmediately(autoCommand + " " + message);
        } else {
            McUtils.sendChat(message);
        }
    }

    public void updateConfig(List<ChatTab> chatTabs) {
        this.chatTabs.clear();
        this.chatTabs.addAll(chatTabs);

        Map<ChatTab, ChatTabData> oldMap = this.tabDataMap;
        this.tabDataMap.clear();
        chatTabs.forEach(chatTab -> this.tabDataMap.put(
                chatTab,
                oldMap.containsKey(chatTab)
                        ? oldMap.get(chatTab)
                        : new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString())));
    }
}
