/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.extension.ItemStackExtension;
import net.minecraft.world.item.ItemStack;

public abstract class GuideItemStack extends ItemStack {
    protected GuideItemStack(ItemStack stack, ItemAnnotation annotation) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());
        ((ItemStackExtension) this).setAnnotation(annotation);
    }
}
