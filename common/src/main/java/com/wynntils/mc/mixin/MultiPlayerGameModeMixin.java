/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    private void handleInventoryMouseClickPre(
            int containerId, int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {

        ItemStack itemStack;
        if (slotId >= 0) {
            itemStack = player.containerMenu.getSlot(slotId).getItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }

        EventFactory.onContainerClickEvent(containerId, slotId, itemStack, clickType, slotId);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void useItemOnPre(
            LocalPlayer player,
            ClientLevel level,
            InteractionHand hand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        EventFactory.onRightClickBlock(player, hand, blockHitResult.getBlockPos(), blockHitResult);
    }
}
