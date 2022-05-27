/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.wc.custom.ItemStackTransformer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @ModifyVariable(
            method = "set(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private ItemStack transformItemStack(ItemStack stack) {
        return ItemStackTransformer.transform(stack);
    }
}
