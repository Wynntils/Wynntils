/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.utils.Utils;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(
            method =
                    "handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void handleInventoryMouseClickPost(
            int containerId,
            int slotNum,
            int buttonNum,
            ClickType clickType,
            Player player,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = player.containerMenu.getSlot(slotNum).getItem();
        Utils.getEventBus()
                .post(
                        new ContainerClickEvent(
                                containerId, slotNum, itemStack, clickType, buttonNum));
    }
}
