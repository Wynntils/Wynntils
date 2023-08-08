/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import net.minecraft.world.item.ItemStack;

public interface ItemAnnotation {
    void onUpdate(ItemStack itemStack);
}
