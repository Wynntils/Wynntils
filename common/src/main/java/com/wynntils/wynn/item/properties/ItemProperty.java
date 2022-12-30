/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;

public abstract class ItemProperty {
    protected final WynnItemStack item;

    protected ItemProperty(WynnItemStack item) {
        this.item = item;

        // attach property to the itemstack
        item.addProperty(this);
    }
}
