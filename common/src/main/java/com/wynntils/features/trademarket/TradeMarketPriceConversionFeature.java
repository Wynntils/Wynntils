/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketPriceConversionFeature extends Feature {
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) \n\uE001 Type the price in emeralds or type \n\uE001 'cancel' to cancel:\n\uE001 ");
    private static final Pattern CANCELLED_PATTERN =
            Pattern.compile("^§4(\uE008\uE002|\uE001) You moved and your chat input was canceled.$");

    private boolean shouldConvert = false;

    @SubscribeEvent
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (event.getOriginalStyledText().stripAlignment().matches(PRICE_PATTERN)) {
            shouldConvert = true;
        }
        if (event.getOriginalStyledText().stripAlignment().matches(CANCELLED_PATTERN)) {
            shouldConvert = false;
        }
    }

    @SubscribeEvent
    public void onClientChat(ChatSentEvent event) {
        if (!shouldConvert) return;
        shouldConvert = false;

        String price = Models.Emerald.convertEmeraldPrice(event.getMessage());
        if (!price.isEmpty()) {
            event.setCanceled(true);
            McUtils.sendChat(price);
        }
    }
}
