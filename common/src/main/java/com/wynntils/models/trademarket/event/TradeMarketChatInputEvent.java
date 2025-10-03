/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.trademarket.type.TradeMarketState;
import net.neoforged.bus.api.Event;

public class TradeMarketChatInputEvent extends Event {
    private final TradeMarketState state;
    private final StyledText message;
    private String response = null;
    private boolean canceled = false;

    public TradeMarketChatInputEvent(TradeMarketState state, StyledText message) {
        this.state = state;
        this.message = message;
    }

    public TradeMarketState getState() {
        return state;
    }

    public StyledText getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void cancelChat() {
        this.canceled = true;
    }
}
