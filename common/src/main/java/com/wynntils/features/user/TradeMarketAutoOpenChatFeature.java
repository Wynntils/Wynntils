/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TradeMarketAutoOpenChatFeature extends UserFeature {
    // Type the price in emeralds or type 'cancel' to cancel:
    // Type the amount you wish to buy or type 'cancel' to cancel:
    // Type the item name or type 'cancel' to cancel:
    private static final Pattern TYPE_TO_CHAT_PATTERN = Pattern.compile("^§6Type the .* or type 'cancel' to cancel:$");

    private boolean openChatWhenContainerClosed = false;

    @SubscribeEvent
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!WynnUtils.onWorld()) return;

        if (TYPE_TO_CHAT_PATTERN.matcher(event.getOriginalCodedMessage()).matches()) {
            openChatWhenContainerClosed = true;
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        if (!openChatWhenContainerClosed) return;

        openChatWhenContainerClosed = false;
        McUtils.mc().setScreen(new ChatScreen(""));
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent event) {
        openChatWhenContainerClosed = false;
    }
}
