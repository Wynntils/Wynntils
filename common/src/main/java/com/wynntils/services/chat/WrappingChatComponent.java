/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.components.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

public class WrappingChatComponent extends ChatComponent {
    public WrappingChatComponent(Minecraft minecraft) {
        super(minecraft);
    }

    @Override
    public void clearMessages(boolean clearSentMsgHistory) {
        Services.ChatTab.clearMessages(clearSentMsgHistory);
    }

    @Override
    public void addMessage(
            Component component, MessageSignature headerSignature, GuiMessageSource source, GuiMessageTag tag) {
        Services.ChatTab.addMessage(component, headerSignature, source, tag);
    }
}
