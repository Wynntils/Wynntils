/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.type.CappedValue;

public class ManaPotionItem extends GameItem implements UsesItemPropery, LeveledItemProperty {
    private final int level;
    private final CappedValue uses;

    public ManaPotionItem(int level, CappedValue uses) {
        this.level = level;
        this.uses = uses;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "ManaPotionItem{" + "level=" + level + ", uses=" + uses + '}';
    }
}
