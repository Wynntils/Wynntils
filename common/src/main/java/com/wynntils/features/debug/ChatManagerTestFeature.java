/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.chat.ChatManager;
import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.ChatMessageReceivedEvent;
import com.wynntils.wc.event.NpcDialogEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatManagerTestFeature extends DebugFeature {
    @Override
    protected boolean onEnable() {
        ChatManager.enableNpcDialogExtraction();
        return super.onEnable();
    }

    @Override
    protected void onDisable() {
        ChatManager.disableNpcDialogExtraction();
        super.onDisable();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatMessage(ChatMessageReceivedEvent e) {
        RecipientType recipientType = e.getRecipientType();
        MessageType messageType = e.getMessageType();

        // TODO: This is a stand-in for per recipientType chat tabs
        e.setMessage(new TextComponent(messageType.name() + "-" + recipientType.name() + ": ").append(e.getMessage()));
    }

    @SubscribeEvent
    public void onNpcDialog(NpcDialogEvent e) {
        String codedDialog = e.getCodedDialog();
        if (codedDialog == null) {
            McUtils.sendMessageToClient(new TextComponent("[NPC dialog removed]"));
            return;
        }

        McUtils.sendMessageToClient(new TextComponent("NPC: " + codedDialog));
    }
}
