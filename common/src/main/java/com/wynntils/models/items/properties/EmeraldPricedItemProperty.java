/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

/**
 * Represents an item that can be bought with emeralds.
 */
public interface EmeraldPricedItemProperty {
    int getEmeraldPrice();

    default boolean hasEmeraldPrice() {
        return getEmeraldPrice() > 0;
    }
}
