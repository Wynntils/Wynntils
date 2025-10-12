/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.config.Config;
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
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;

public final class ChatTabService extends Service {
    private final Map<ChatTab, ChatTabData> tabDataMap = new HashMap<>();
    // This is a copy of the config in ChatTabsFeature, stored there for persistence.
    private final List<ChatTab> chatTabs = new ArrayList<>();

    private ChatComponent vanillaChatComponent = null;
    private ChatTab focusedTab = null;

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

        Config<List<ChatTab>> configChatTabs = Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs;
        configChatTabs.get().add(insertIndex, chatTab);
        configChatTabs.touched();

        tabDataMap.put(chatTab, new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString()));
    }

    public void removeTab(ChatTab chatTab) {
        getChatTabs().remove(chatTab);

        Config<List<ChatTab>> configChatTabs = Managers.Feature.getFeatureInstance(ChatTabsFeature.class).chatTabs;
        configChatTabs.get().remove(chatTab);
        configChatTabs.touched();

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

        ChatTabData focusedChatTabData = tabDataMap.get(focused);
        focusedChatTabData.setUnreadMessages(false);

        // Copy the focused tabs messages into the wrapping chat component for display
        McUtils.mc().gui.chat.allMessages = focusedChatTabData.getChatComponent().allMessages;
        McUtils.mc().gui.chat.trimmedMessages = focusedChatTabData.getChatComponent().trimmedMessages;
        McUtils.mc().gui.chat.refreshTrimmedMessages();
    }
    // endregion

    // region Enable/disable

    public void enable() {
        if (isEnabled()) return;

        if (getTabCount() == 0) {
            WynntilsMod.warn("Cannot enable Chat Tabs: no tabs configured");
            return;
        }

        reset();

        // Create a new ChatTabData for each tab
        getChatTabs()
                .forEach(chatTab -> tabDataMap.put(
                        chatTab, new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString())));

        // Pass the historic messages from the vanilla chat component to all the new tabs
        McUtils.mc().gui.chat.allMessages.reversed().forEach(msg -> {
            Component component = msg.content();
            StyledText styledText = StyledText.fromComponent(component);
            RecipientType recipientType = Handlers.Chat.getRecipientType(styledText, MessageType.FOREGROUND);
            List<ChatTab> recipientTabs = Services.ChatTab.getRecipientTabs(recipientType, styledText);

            recipientTabs.forEach(tab -> Services.ChatTab.getChatComponent(tab).addMessage(component));
        });

        vanillaChatComponent = McUtils.mc().gui.chat;
        McUtils.mc().gui.chat = new WrappingChatComponent(McUtils.mc());

        setFocusedTab(getChatTabs().getFirst());
    }

    public void disable() {
        if (!isEnabled()) return;

        McUtils.mc().gui.chat = vanillaChatComponent;
        vanillaChatComponent = null;

        reset();
        focusedTab = null;
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

    void clearMessages(boolean clearSentMsgHistory) {
        vanillaChatComponent.clearMessages(clearSentMsgHistory);
        tabDataMap
                .values()
                .forEach(chatTabData -> chatTabData.getChatComponent().clearMessages(clearSentMsgHistory));
    }

    void addMessage(Component component, MessageSignature headerSignature, GuiMessageTag tag) {
        try {
            vanillaChatComponent.addMessage(component, headerSignature, tag);

            StyledText styledText = StyledText.fromComponent(component);
            RecipientType recipientType = Handlers.Chat.getRecipientType(styledText, MessageType.FOREGROUND);

            List<ChatTab> recipientTabs = getRecipientTabs(recipientType, styledText);
            recipientTabs.forEach(tab -> {
                getChatComponent(tab).addMessage(component);
                markAsNewMessages(tab);
            });
        } catch (Throwable t) {
            warnAboutBrokenMod(component, t);
        }
    }

    private void warnAboutBrokenMod(Component component, Throwable t) {
        MutableComponent warning = Component.literal(
                        "<< WARNING: A chat message was lost due to a crash in a mod other than Wynntils. See log for details. >>")
                .withStyle(ChatFormatting.RED);
        vanillaChatComponent.addMessage(warning);
        getChatComponent(focusedTab).addMessage(warning);

        // We have seen many issues with badly written mods that inject into addMessage, and
        // throws exceptions. Instead of considering it a Wynntils crash, dump it to the log and
        // ignore it. We can't resend the message to the chat, since that could cause an infinite loop,
        // but the log should be fine.
        WynntilsMod.warn("Another mod has caused an exception in ChatComponent.addMessage()");
        WynntilsMod.warn("The message that could not be displayed is:"
                + StyledText.fromComponent(component).getString());
        WynntilsMod.warn("This is not a Wynntils bug. Here is the exception that we caught.", t);
    }

    private void reset() {
        tabDataMap.clear();
    }

    private void markAsNewMessages(ChatTab tab) {
        if (tab != focusedTab) {
            tabDataMap.get(tab).setUnreadMessages(true);
        }
    }

    private List<ChatTab> getRecipientTabs(RecipientType recipientType, StyledText styledText) {
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

    public void modifyChatHistory(Consumer<List<GuiMessage>> allMessagesConsumer) {
        if (!isEnabled()) {
            ChatComponent chatComponent = McUtils.mc().gui.chat;

            allMessagesConsumer.accept(chatComponent.allMessages);
            chatComponent.refreshTrimmedMessages();
            return;
        }

        Stream.concat(
                        Stream.of(this.vanillaChatComponent),
                        tabDataMap.values().stream().map(ChatTabData::getChatComponent))
                .forEach(chatComponent -> {
                    allMessagesConsumer.accept(chatComponent.allMessages);
                    chatComponent.refreshTrimmedMessages();
                });
    }

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

    public void setChatTabs(List<ChatTab> chatTabs) {
        this.chatTabs.clear();
        this.chatTabs.addAll(chatTabs);

        Map<ChatTab, ChatTabData> oldMap = new HashMap<>(this.tabDataMap);
        this.tabDataMap.clear();
        chatTabs.forEach(chatTab -> this.tabDataMap.put(
                chatTab,
                oldMap.containsKey(chatTab)
                        ? oldMap.get(chatTab)
                        : new ChatTabData(new ChatComponent(McUtils.mc()), false, chatTab.customRegexString())));

        if (focusedTab != null) {
            setFocusedTab(chatTabs.getFirst());
        }
    }
}
