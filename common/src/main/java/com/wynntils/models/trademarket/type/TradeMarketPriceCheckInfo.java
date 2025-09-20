/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket.type;

/**
 * Represents the information from the price check slot of a trade market item.
 *
 * @param recommendedPrice The recommended price
 * @param bid The highest buy offer, or -1 if not available.
 * @param ask The lowest sell offer, or -1 if not available.
 */
public record TradeMarketPriceCheckInfo(int recommendedPrice, int bid, int ask) {
    public static final TradeMarketPriceCheckInfo EMPTY = new TradeMarketPriceCheckInfo(0, -1, -1);
}
