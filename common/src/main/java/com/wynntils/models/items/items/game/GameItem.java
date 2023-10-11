/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.EmeraldPricedItemProperty;

public abstract class GameItem extends WynnItem implements EmeraldPricedItemProperty {
    protected final int emeraldPrice;

    protected GameItem(int emeraldPrice) {
        this.emeraldPrice = emeraldPrice;
    }

    @Override
    public int getEmeraldPrice() {
        return emeraldPrice;
    }
}
