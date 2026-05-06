/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket.type;

public enum TradeMarketState {
    NOT_ACTIVE,
    DEFAULT_RESULTS,
    FILTERS_PAGE,
    FILTERED_RESULTS,
    SELLING,
    BUYING,
    VIEWING_TRADES,
    VIEWING_ORDER,
    SEARCH_CHAT_INPUT,
    AMOUNT_CHAT_INPUT,
    PRICE_CHAT_INPUT;

    public boolean isResults() {
        return this == DEFAULT_RESULTS || this == FILTERED_RESULTS;
    }
}
