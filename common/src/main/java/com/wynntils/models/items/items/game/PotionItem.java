/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.PotionType;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class PotionItem extends GameItem implements UsesItemPropery {
    private final PotionType type;
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public PotionItem(PotionType type, List<ItemEffect> effects, CappedValue uses) {
        this.type = type;
        this.effects = effects;
        this.uses = uses;
    }

    public PotionType getType() {
        return type;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "PotionItem{" + "type=" + type + ", effects=" + effects + ", uses=" + uses + '}';
    }
}
