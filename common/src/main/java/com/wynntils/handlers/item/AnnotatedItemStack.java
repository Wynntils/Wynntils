/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import net.minecraft.world.item.ItemStack;

public class AnnotatedItemStack extends ItemStack {
    private final ItemAnnotation annotation;

    public AnnotatedItemStack(ItemStack itemStack, ItemAnnotation annotation) {
        super(itemStack.getItem(), itemStack.getCount());
        if (itemStack.getTag() != null) {
            setTag(itemStack.getTag());
        }

        this.annotation = annotation;
    }

    public ItemAnnotation getAnnotation() {
        return annotation;
    }
}
