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
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatManagerTestFeature extends DebugFeature {
    @Override
    protected boolean onEnable() {
        ChatManager.enableNpcDialogExtraction();
        return super.onEnable();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getRecipientType() == RecipientType.INFO) return;

        RecipientType recipientType = e.getRecipientType();
        MessageType messageType = e.getMessageType();

        // TODO: This is a stand-in for per recipientType chat tabs
        e.setMessage(new TextComponent(messageType.name() + "-" + recipientType.name() + ": ").append(e.getMessage()));
    }

    @SubscribeEvent
    public void onNpcDialog(NpcDialogEvent e) {
        if (!WynnUtils.onServer()) return;

        List<String> codedDialogLines = e.getCodedDialogLines();
        if (codedDialogLines.isEmpty()) {
            McUtils.sendMessageToClient(new TextComponent("[NPC dialog removed]"));
            return;
        }

        for (String dialogLine : codedDialogLines) {
            McUtils.sendMessageToClient(new TextComponent("NPC: " + dialogLine));
        }
    }
}
