/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class ManaPotionItem extends GameItem implements UsesItemPropery {
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public ManaPotionItem(List<ItemEffect> effects, CappedValue uses) {
        this.effects = effects;
        this.uses = uses;
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
        return "ManaPotionItem{" + "effects=" + effects + ", uses=" + uses + '}';
    }
}
