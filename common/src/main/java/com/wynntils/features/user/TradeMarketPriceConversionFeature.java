package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Pattern;

public class TradeMarketPriceConversionFeature extends UserFeature {

    private static final Pattern PRICE_PATTERN = Pattern.compile("^§6Type the price in emeralds or type 'cancel' to cancel:$");

    private boolean shouldConvert = false;

    @SubscribeEvent
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if(!WynnUtils.onWorld()) return;

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
