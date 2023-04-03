/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.utils.mc.type.CodedString;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemAnnotator {
    ItemAnnotation getAnnotation(ItemStack itemStack, CodedString name);
}
