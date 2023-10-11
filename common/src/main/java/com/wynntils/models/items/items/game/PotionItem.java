/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.PotionType;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.UsesItemProperty;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class PotionItem extends GameItem implements UsesItemProperty, LeveledItemProperty {
    private final PotionType type;
    private final int level;
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public PotionItem(int emeraldPrice, PotionType type, int level, List<ItemEffect> effects, CappedValue uses) {
        super(emeraldPrice);
        this.type = type;
        this.level = level;
        this.effects = effects;
        this.uses = uses;
    }

    public PotionType getType() {
        return type;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    @Override
    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "PotionItem{" + "type="
                + type + ", level="
                + level + ", effects="
                + effects + ", uses="
                + uses + ", emeraldPrice="
                + emeraldPrice + '}';
    }
}
