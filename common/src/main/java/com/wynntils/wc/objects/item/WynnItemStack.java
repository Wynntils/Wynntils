/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public abstract class WynnItemStack extends ItemStack {

    protected String itemName;

    protected WynnItemStack(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());

        itemName = WynnUtils.normalizeBadString(
                ChatFormatting.stripFormatting(getHoverName().getString()));
    }
}
