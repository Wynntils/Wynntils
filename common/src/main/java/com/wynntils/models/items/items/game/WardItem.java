/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;

public class WardItem extends GameItem {
    private final StyledText name;

    public WardItem(StyledText name) {
        this.name = name;
    }

    public StyledText getName() {
        return name;
    }

    @Override
    public String toString() {
        return "WardItem{" + "name=" + name.getString() + '}';
    }
}
