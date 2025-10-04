/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket.event;

import com.wynntils.models.trademarket.type.TradeMarketState;
import net.neoforged.bus.api.Event;

public class TradeMarketStateEvent extends Event {
    private final TradeMarketState newState;
    private final TradeMarketState oldState;

    public TradeMarketStateEvent(TradeMarketState newState, TradeMarketState oldState) {
        this.newState = newState;
        this.oldState = oldState;
    }

    public TradeMarketState getNewState() {
        return newState;
    }

    public TradeMarketState getOldState() {
        return oldState;
    }
}
