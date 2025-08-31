/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin.accessors;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HoverEvent.ItemStackInfo.class)
public interface ItemStackInfoAccessor {
    @Accessor("itemStack")
    void setItemStack(ItemStack itemStack);
}
