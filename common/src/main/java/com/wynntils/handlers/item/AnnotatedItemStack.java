/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import net.minecraft.world.item.ItemStack;

public class AnnotatedItemStack extends ItemStack {
    private final ItemAnnotation annotation;

    public AnnotatedItemStack(ItemStack stack, ItemAnnotation annotation) {
        super(stack.getItem(), stack.getCount());
        this.annotation = annotation;
    }

    public ItemAnnotation getAnnotation() {
        return annotation;
    }
}
