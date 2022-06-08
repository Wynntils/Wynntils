/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.wc.custom.item.WynnItemStack;

public abstract class ItemProperty {
    public static final Class<DurabilityProperty> DURABILITY = DurabilityProperty.class;

    protected WynnItemStack stack;

    public ItemProperty(WynnItemStack stack) {
        this.stack = stack;

        // attach property to the stack
        stack.addProperty(this);
    }
}
