package com.wynntils.models.trademarket.type;

public enum TradeMarketSortOrder {
    MOST_RECENT,
    LEAST_RECENT,
    MOST_EXPENSIVE,
    LEAST_EXPENSIVE,
    HIGHEST_LEVEL_RANGE,
    LOWEST_LEVEL_RANGE;

    public static final int LENGTH = values().length;
}