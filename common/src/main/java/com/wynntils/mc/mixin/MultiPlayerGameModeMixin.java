/*
 * Copyright © Wynntils 2022-2023.
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
    @Inject(
            method =
                    "handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleInventoryMouseClickPre(
            int containerId, int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {
        if (containerId != player.containerMenu.containerId) return;

        ContainerClickEvent event = new ContainerClickEvent(player.containerMenu, slotId, clickType, mouseButton);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void useItemOnPre(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult result,
            CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.RightClickBlock event =
                new PlayerInteractEvent.RightClickBlock(player, hand, result.getBlockPos(), result);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(
            method =
                    "useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void useItemPre(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        UseItemEvent event = new UseItemEvent(player, McUtils.mc().level, hand);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(
            method =
                    "interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void interactAt(
            Player player,
            Entity target,
            EntityHitResult ray,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.InteractAt event = new PlayerInteractEvent.InteractAt(player, hand, target, ray);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(
            method =
                    "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void interact(
            Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.Interact event = new PlayerInteractEvent.Interact(player, hand, target);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(
            method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void attack(Player player, Entity target, CallbackInfo ci) {
        PlayerAttackEvent event = new PlayerAttackEvent(player, target);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    // As of 1.19.3, this seems to be the only method which sends carried item update packets to the server.
    // Please look into this and confirm this is still the case, in future versions.
    @Inject(
            method = "ensureHasSentCarriedItem()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void ensureHasSentCarriedItem(CallbackInfo ci) {
        MixinHelper.post(new ChangeCarriedItemEvent());
    }
}
