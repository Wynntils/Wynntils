/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.SetSlotEvent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @WrapOperation(
            method = "set(Lnet/minecraft/world/item/ItemStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void onSetItem(Container container, int slot, ItemStack itemStack, Operation<Void> original) {
        SetSlotEvent.Pre event = new SetSlotEvent.Pre(container, slot, itemStack);
        MixinHelper.post(event);

        ItemStack oldItemStack = container.getItem(slot);
        original.call(container, slot, event.getItemStack());

        MixinHelper.post(new SetSlotEvent.Post(container, slot, event.getItemStack(), oldItemStack));
    }
}
