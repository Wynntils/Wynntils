/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item.render;

import net.minecraft.world.item.ItemStack;

public interface RenderedHotbarForeground {
    void renderHotbarForeground(int x, int y, ItemStack stack);
}
