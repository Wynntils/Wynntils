/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

import com.wynntils.handlers.container.ContainerQueryException;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ContainerContentUpdatePredicate {
    boolean execute(ContainerContent container, Int2ObjectArrayMap<ItemStack> updatedItems)
            throws ContainerQueryException;
}
