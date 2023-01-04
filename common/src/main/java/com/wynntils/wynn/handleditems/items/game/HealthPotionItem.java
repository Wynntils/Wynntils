/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;

public class HealthPotionItem extends GameItem implements UsesItemPropery {
    private final int hearts;
    private final CappedValue uses;

    public HealthPotionItem(int hearts, CappedValue uses) {
        this.hearts = hearts;
        this.uses = uses;
    }

    public int getHearts() {
        return hearts;
    }

    @Override
    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "HealthPotionItem{" + "hearts=" + hearts + ", uses=" + uses + '}';
    }
}
