/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.handlers.item.ItemAnnotation;
import net.minecraft.world.item.ItemStack;

public class WynnItem implements ItemAnnotation {
    private final WynnItemData data = new WynnItemData();

    public WynnItemData getData() {
        return data;
    }

    @Override
    public String toString() {
        return "WynnItem{}";
    }

    @Override
    public void onUpdate(ItemStack itemStack) {
        data.clearAll();
        data.store(WynnItemData.ITEMSTACK_KEY, itemStack);
    }
}
