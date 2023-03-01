/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.wynnitem.type.ItemEffect;
import java.util.List;

public class XpPotionItem extends GameItem {
    private final List<ItemEffect> effects;

    public XpPotionItem(List<ItemEffect> effects) {
        this.effects = effects;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        return "XpPotionItem{" + "effects=" + effects + '}';
    }
}
