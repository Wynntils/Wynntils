/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat;

import com.wynntils.core.components.Services;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
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
    public void addMessage(Component component, MessageSignature headerSignature, GuiMessageTag tag) {
        Services.ChatTab.addMessage(component, headerSignature, tag);
    }
}
