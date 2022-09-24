/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.alwayson;

import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TradeMarketPriceConversionFeature extends StateManagedFeature {

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("^§6Type the price in emeralds or type 'cancel' to cancel:$");

    private boolean shouldConvert = false;

    @SubscribeEvent
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!WynnUtils.onWorld()) return;

        if (PRICE_PATTERN.matcher(event.getCodedMessage()).matches()) {
            shouldConvert = true;
        }
    }

    @SubscribeEvent
    public void onClientChat(ChatSentEvent event) {
        if (!shouldConvert) return;
        shouldConvert = false;

        String price = StringUtils.convertEmeraldPrice(event.getMessage());
        if (!price.isEmpty()) event.setMessage(price);
    }
}
