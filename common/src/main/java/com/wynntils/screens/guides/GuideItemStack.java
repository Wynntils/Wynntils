/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Handlers;
import com.wynntils.handlers.item.ItemAnnotation;
import net.minecraft.world.item.ItemStack;

public abstract class GuideItemStack extends ItemStack {
    protected GuideItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName) {
        super(itemStack.getItem(), 1);
        this.setTag(itemStack.getTag());
        Handlers.Item.updateItem(this, annotation, baseName);
    }
}
