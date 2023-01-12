/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    private void handleInventoryMouseClickPre(
            int containerId, int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {

        ItemStack itemStack;
        if (slotId >= 0) {
            itemStack = player.containerMenu.getSlot(slotId).getItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }

        if (EventFactory.onContainerClickEvent(containerId, slotId, itemStack, clickType, slotId)
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void useItemOnPre(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult result,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (EventFactory.onRightClickBlock(player, hand, result.getBlockPos(), result)
                .isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void useItemPre(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (EventFactory.onUseItem(player, McUtils.mc().level, hand).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void interactAt(
            Player player,
            Entity target,
            EntityHitResult ray,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (EventFactory.onInteractAt(player, hand, target, ray).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(
            Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (EventFactory.onInteract(player, hand, target).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Player player, Entity target, CallbackInfo ci) {
        if (EventFactory.onAttack(player, target).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "ensureHasSentCarriedItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void ensureHasSentCarriedItem(CallbackInfo ci) {
        EventFactory.onChangeCarriedItemEvent();
    }
}
