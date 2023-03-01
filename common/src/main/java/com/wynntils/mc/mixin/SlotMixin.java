/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.SetSlotEvent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Redirect(
            method = "set(Lnet/minecraft/world/item/ItemStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void redirectSetItem(Container container, int slot, ItemStack itemStack) {
        SetSlotEvent result = EventFactory.onSetSlotPre(container, slot, itemStack);
        if (result.isCanceled()) return;

        container.setItem(slot, result.getItemStack());

        EventFactory.onSetSlotPost(container, slot, itemStack);
    }
}
