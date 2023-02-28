/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.LeveledItemProperty;

public class XpPotionItem extends GameItem implements LeveledItemProperty {
    private final int level;

    public XpPotionItem(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "XpPotionItem{" + "level=" + level + '}';
    }
}
