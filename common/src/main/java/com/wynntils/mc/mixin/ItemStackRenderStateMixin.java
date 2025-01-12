/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.ItemStackRenderStateExtension;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements ItemStackRenderStateExtension {
    @Unique
    private ItemStack itemStack;

    @Unique
    @Override
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Unique
    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
