/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
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
        if (containerId != player.containerMenu.containerId) return;

        if (MixinHelper.post(new ContainerClickEvent(player.containerMenu, slotId, clickType, mouseButton))
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
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, result.getBlockPos(), result);
        if (MixinHelper.post(event).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void useItemPre(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (MixinHelper.post(new UseItemEvent(player, McUtils.mc().level, hand)).isCanceled()) {
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
        PlayerInteractEvent.InteractAt event = new PlayerInteractEvent.InteractAt(player, hand, target, ray);
        if (MixinHelper.post(event).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(
            Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.Interact event = new PlayerInteractEvent.Interact(player, hand, target);
        if (MixinHelper.post(event).isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Player player, Entity target, CallbackInfo ci) {
        if (MixinHelper.post(new PlayerAttackEvent(player, target)).isCanceled()) {
            ci.cancel();
        }
    }

    // As of 1.19.3, this seems to be the only method which sends carried item update packets to the server.
    // Please look into this and confirm this is still the case, in future versions.
    @Inject(
            method = "ensureHasSentCarriedItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void ensureHasSentCarriedItem(CallbackInfo ci) {
        MixinHelper.post(new ChangeCarriedItemEvent());
    }
}
