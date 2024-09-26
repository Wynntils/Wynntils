/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ContainerLimitStackSizeEvent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleContainer.class)
public class SimpleContainerMixin {
    @WrapOperation(
            method = "setItem(ILnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;limitSize(I)V"))
    private void onLimitSize(ItemStack stack, int limit, Operation<Void> original, int slot) {
        ContainerLimitStackSizeEvent event =
                new ContainerLimitStackSizeEvent((SimpleContainer) (Object) this, slot, stack.copy(), limit);
        MixinHelper.post(event);

        if (!event.isCanceled()) {
            original.call(stack, event.getLimit());
        }
    }
}
