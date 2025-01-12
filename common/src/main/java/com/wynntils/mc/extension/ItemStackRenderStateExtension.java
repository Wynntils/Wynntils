/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import net.minecraft.world.item.ItemStack;

public interface ItemStackRenderStateExtension {
    void setItemStack(ItemStack itemStack);

    ItemStack getItemStack();
}
