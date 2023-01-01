/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.NumberedTierItemProperty;

public class EmeraldPouchItem extends GameItem implements NumberedTierItemProperty {
    private final int tier;

    public EmeraldPouchItem(int tier) {
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public String toString() {
        return "EmeraldPouchItem{" + "tier=" + tier + '}';
    }
}
