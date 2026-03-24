/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.extension.ItemStackExtension;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtension {
    @Unique
    private ItemAnnotation wynntilsAnnotation;

    @Unique
    private StyledText wynntilsOriginalName;

    @Override
    @Unique
    public ItemAnnotation getAnnotation() {
        return wynntilsAnnotation;
    }

    @Override
    @Unique
    public void setAnnotation(ItemAnnotation annotation) {
        this.wynntilsAnnotation = annotation;
    }

    @Override
    @Unique
    public StyledText getOriginalName() {
        return this.wynntilsOriginalName;
    }

    @Override
    @Unique
    public void setOriginalName(StyledText name) {
        this.wynntilsOriginalName = name;
    }
}
