/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.gui.SoulPointItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import net.minecraft.world.item.ItemStack;

public final class SoulPointAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        if (!WynnItemMatchers.isSoulPoint(itemStack)) return null;

        return new SoulPointItem();
    }
}
