/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.rewards.type.WardType;

public class WardItem extends GameItem {
    private final WardType type;

    public WardItem(WardType type) {
        this.type = type;
    }

    public WardType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "WardItem{" + "type=" + type + '}';
    }
}
