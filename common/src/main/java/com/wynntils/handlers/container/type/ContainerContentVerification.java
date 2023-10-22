/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.type;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ContainerContentVerification {
    boolean verify(ContainerContent container, Int2ObjectMap<ItemStack> changes, ContainerContentChangeType changeType);
}
