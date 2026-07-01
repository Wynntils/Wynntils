/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.utils.EnumUtils;

public class WardItem extends GameItem implements NamedItemProperty {
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

    @Override
    public String getName() {
        return EnumUtils.toNiceString(type) + " Ward";
    }
}
