/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket.type;

/**
 * Represents the price information of a trade market item.
 * @param price The price of one item.
 * @param silverbullPrice The price of one item for silverbull subscribers. Same as price if the item is not silverbull discounted.
 * @param amount The number of items being sold.
 */
public record TradeMarketPriceInfo(int price, int silverbullPrice, int amount) {
    public static final TradeMarketPriceInfo EMPTY = new TradeMarketPriceInfo(0, 0, 0);

    public int totalPrice() {
        return silverbullPrice * amount;
    }
}
