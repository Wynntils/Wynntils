/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

public enum BombSortOrder {
    NEWEST,
    OLDEST;

    public static BombSortOrder fromString(String order) {
        for (BombSortOrder sortOrder : values()) {
            if (order.equalsIgnoreCase(sortOrder.name())) return sortOrder;
        }
        return NEWEST;
    }
}
