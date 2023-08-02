/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.chat.ChatTabsFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ChatTabService extends Service {
    private final ChatComponent FALLBACK_CHAT = new ChatComponent(McUtils.mc());

    private ChatTab focusedTab = null;

    private final Map<ChatTab, ChatComponent> chatTabData = new ConcurrentHashMap<>();
    private final Map<ChatTab, Boolean> unreadMessages = new ConcurrentHashMap<>();

    public ChatTabService() {
        super(List.of());
    }

    private List<ChatTab> getChatTabs() {
        return Managers.Feature.getFeatureInstance(ChatTabsFeature.class)
                .chatTabs
                .get();
    }

    public Stream<ChatTab> getTabs() {
        return getChatTabs().stream();
    }

    public ChatTab getTab(int index) {
        return getChatTabs().get(index);
    }

    public int getTabCount() {
        return getChatTabs().size();
    }

    public boolean isTabListEmpty() {
        return getTabCount() == 0;
    }

    public void addTab(int insertIndex, ChatTab chatTab) {
        getChatTabs().add(insertIndex, chatTab);
        Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs.touched();
    }

    public void removeTab(ChatTab chatTab) {
        getChatTabs().remove(chatTab);
        Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs.touched();
    }

    public int getTabIndex(ChatTab edited) {
        return getChatTabs().indexOf(edited);
    }

    public int getNextFocusedTab() {
        return (getTabIndex(getFocusedTab()) + 1) % getTabCount();
    }

    public void refocusFirstTab() {
        if (!isTabListEmpty()) {
            setFocusedTab(0);
        }
    }

    public void resetFocusedTab() {
        setFocusedTab(null);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            chatTabData.clear();
            unreadMessages.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        chatTabData.values().forEach(ChatComponent::tick);
    }

    public void setFocusedTab(int index) {
        setFocusedTab(getTab(index));
    }

    public void setFocusedTab(ChatTab focused) {
        focusedTab = focused;

        if (focusedTab == null) {
            McUtils.mc().gui.chat = FALLBACK_CHAT;
        } else {
            chatTabData.putIfAbsent(focusedTab, new ChatComponent(McUtils.mc()));
            unreadMessages.put(focusedTab, false);
            McUtils.mc().gui.chat = chatTabData.get(focusedTab);
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
        for (ChatTab chatTab : getChatTabs()) {
            if (!chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getComponent());
                return;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : getChatTabs()) {
            if (chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getComponent());
            }
        }
    }

    public void matchMessage(ChatMessageReceivedEvent event) {
        // Firstly, find the FIRST matching tab with high priority
        for (ChatTab chatTab : getChatTabs()) {
            if (!chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getMessage());
                return;
            }
        }

        // Secondly, match ALL tabs with low priority
        for (ChatTab chatTab : getChatTabs()) {
            if (chatTab.isConsuming()) continue;

            if (matchMessageFromEvent(chatTab, event)) {
                addMessageToTab(chatTab, event.getMessage());
            }
        }
    }

    private void addMessageToTab(ChatTab tab, Component message) {
        chatTabData.putIfAbsent(tab, new ChatComponent(McUtils.mc()));

        try {
            chatTabData.get(tab).addMessage(message);
        } catch (Throwable t) {
            MutableComponent warning = Component.literal(
                            "<< WARNING: A chat message was lost due to a crash in a mod other than Wynntils. See log for details. >>")
                    .withStyle(ChatFormatting.RED);
            chatTabData.get(tab).addMessage(warning);
            // We have seen many issues with badly written mods that inject into addMessage, and
            // throws exceptions. Instead of considering it a Wynntils crash, dump it to the log and
            // ignore it. We can't resend the message to the chat, since that could cause an infinite loop,
            // but the log should be fine.
            WynntilsMod.warn("Another mod has caused an exception in ChatComponent.addMessage()");
            WynntilsMod.warn("The message that could not be displayed is:"
                    + StyledText.fromComponent(message).getString());
            WynntilsMod.warn("This is not a Wynntils bug. Here is the exception that we caught.", t);
        }

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

        Optional<Pattern> regex = chatTab.getCustomRegex();
        return regex.isEmpty() || event.getOriginalStyledText().matches(regex.get());
    }

    private boolean matchMessageFromEvent(ChatTab chatTab, ClientsideMessageEvent event) {
        if (chatTab.getFilteredTypes() != null
                && !chatTab.getFilteredTypes().isEmpty()
                && !chatTab.getFilteredTypes().contains(RecipientType.CLIENTSIDE)) {
            return false;
        }

        Optional<Pattern> regex = chatTab.getCustomRegex();
        if (regex.isEmpty()) return true;

        return event.getOriginalStyledText().matches(regex.get());
    }
}
