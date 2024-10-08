/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketAutoOpenChatFeature extends Feature {
    // Test in TradeMarketAutoOpenChatFeature_TYPE_TO_CHAT_PATTERN

    @Persisted
    public final Config<Boolean> hidePrompt = new Config<>(false);

    private static final Pattern TYPE_TO_CHAT_PATTERN =
            Pattern.compile("^§5(\uE00A\uE002|\uE001) Type the .* or type 'cancel' to cancel:");

    private boolean openChatWhenContainerClosed = false;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!Models.WorldState.onWorld()) return;
        StyledText styledText =
                StyledTextUtils.unwrap(event.getOriginalStyledText()).stripAlignment();

        if (styledText.matches(TYPE_TO_CHAT_PATTERN)) {
            openChatWhenContainerClosed = true;
            if (hidePrompt.get()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        if (!openChatWhenContainerClosed) return;

        openChatWhenContainerClosed = false;
        McUtils.mc().setScreen(new ChatScreen(""));
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        openChatWhenContainerClosed = false;
    }
}
