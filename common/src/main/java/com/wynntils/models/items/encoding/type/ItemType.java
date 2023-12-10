/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemType {
    GEAR(0);

    private final int id;

    ItemType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
