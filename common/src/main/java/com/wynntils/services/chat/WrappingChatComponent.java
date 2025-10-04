/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.services.chat.type.ChatTab;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ArrayListDeque;

public class WrappingChatComponent extends ChatComponent {
    ChatComponent originalChatComponent;
    ChatComponent currentChatComponent;

    public WrappingChatComponent(Minecraft minecraft) {
        super(minecraft);
        originalChatComponent = minecraft.gui.chat;
    }

    public void setCurrentChatComponent(ChatComponent chatComponent) {
        this.currentChatComponent = chatComponent;
    }

    public ChatComponent getOriginalChatComponent() {
        return originalChatComponent;
    }

    @Override
    public void tick() {
        originalChatComponent.tick();
        Services.ChatTab.forEachChatComponent(ChatComponent::tick);
    }

    @Override
    public void clearMessages(boolean clearSentMsgHistory) {
        originalChatComponent.clearMessages(clearSentMsgHistory);
        Services.ChatTab.forEachChatComponent(chatComponent -> chatComponent.clearMessages(clearSentMsgHistory));
    }

    @Override
    public void deleteMessage(MessageSignature messageSignature) {
        originalChatComponent.deleteMessage(messageSignature);
        Services.ChatTab.forEachChatComponent(chatComponent -> chatComponent.deleteMessage(messageSignature));
    }

    @Override
    public void rescaleChat() {
        originalChatComponent.rescaleChat();
        Services.ChatTab.forEachChatComponent(ChatComponent::rescaleChat);
    }

    @Override
    public void addRecentChat(String message) {
        originalChatComponent.addRecentChat(message);
        Services.ChatTab.forEachChatComponent(chatComponent -> chatComponent.addRecentChat(message));
    }

    @Override
    public void resetChatScroll() {
        originalChatComponent.resetChatScroll();
        Services.ChatTab.forEachChatComponent(ChatComponent::resetChatScroll);
    }

    @Override
    public State storeState() {
        return originalChatComponent.storeState();
    }

    @Override
    public void restoreState(State state) {
        originalChatComponent.restoreState(state);
    }

    @Override
    public ArrayListDeque<String> getRecentChat() {
        return currentChatComponent.getRecentChat();
    }

    @Override
    public void scrollChat(int posInc) {
        currentChatComponent.scrollChat(posInc);
    }

    @Override
    public boolean handleChatQueueClicked(double mouseX, double mouseY) {
        return currentChatComponent.handleChatQueueClicked(mouseX, mouseY);
    }

    @Override
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        return currentChatComponent.getClickedComponentStyleAt(mouseX, mouseY);
    }

    @Override
    public GuiMessageTag getMessageTagAt(double mouseX, double mouseY) {
        return currentChatComponent.getMessageTagAt(mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused) {
        currentChatComponent.render(guiGraphics, tickCount, mouseX, mouseY, focused);
    }

    @Override
    public void addMessage(Component component, MessageSignature headerSignature, GuiMessageTag tag) {
        try {
            originalChatComponent.addMessage(component, headerSignature, tag);

            StyledText styledText = StyledText.fromComponent(component);
            RecipientType recipientType = Handlers.Chat.getRecipientType(styledText, MessageType.FOREGROUND);

            List<ChatTab> recipientTabs = Services.ChatTab.getRecipientTabs(recipientType, styledText);
            recipientTabs.forEach(tab -> {
                Services.ChatTab.getChatComponent(tab).addMessage(component);
                Services.ChatTab.markAsNewMessages(tab);
            });
        } catch (Throwable t) {
            warnAboutBrokenMod(component, t);
        }
    }

    private void warnAboutBrokenMod(Component component, Throwable t) {
        MutableComponent warning = Component.literal(
                        "<< WARNING: A chat message was lost due to a crash in a mod other than Wynntils. See log for details. >>")
                .withStyle(ChatFormatting.RED);
        originalChatComponent.addMessage(warning);
        currentChatComponent.addMessage(warning);
        // We have seen many issues with badly written mods that inject into addMessage, and
        // throws exceptions. Instead of considering it a Wynntils crash, dump it to the log and
        // ignore it. We can't resend the message to the chat, since that could cause an infinite loop,
        // but the log should be fine.
        WynntilsMod.warn("Another mod has caused an exception in ChatComponent.addMessage()");
        WynntilsMod.warn("The message that could not be displayed is:"
                + StyledText.fromComponent(component).getString());
        WynntilsMod.warn("This is not a Wynntils bug. Here is the exception that we caught.", t);
    }
}
