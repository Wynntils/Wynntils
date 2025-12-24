/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.utils.mc.McUtils;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketPriceConversionFeature extends Feature {
    public TradeMarketPriceConversionFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onChatSent(ChatSentEvent event) {
        if (Models.TradeMarket.getTradeMarketState() != TradeMarketState.PRICE_CHAT_INPUT) return;

        String price = Models.Emerald.convertEmeraldPrice(event.getMessage());
        if (!price.isEmpty()) {
            event.setCanceled(true);
            McUtils.sendChat(price);
        }
    }
}
