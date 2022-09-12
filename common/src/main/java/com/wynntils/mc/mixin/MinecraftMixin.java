/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ArmSwingEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    private void setScreenPostPost(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            EventFactory.onScreenClose();
        } else {
            EventFactory.onScreenOpened(screen);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickPre(CallbackInfo ci) {
        EventFactory.onTickStart();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickPost(CallbackInfo ci) {
        EventFactory.onTickEnd();
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    public void resizeDisplayPost(CallbackInfo ci) {
        EventFactory.onResizeDisplayPost();
    }

    @Redirect(
            method = "startUseItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V",
                            ordinal = 0))
    private void swingOnInteractWithEntity(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.INTERACT_WITH_ENTITY, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }

    @Redirect(
            method = "startUseItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V",
                            ordinal = 1))
    private void swingOnInteractWithBlock(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.INTERACT_WITH_BLOCK, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }

    @Redirect(
            method = "startUseItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V",
                            ordinal = 2))
    private void swingOnUseItem(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.USE_ITEM, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }

    @Redirect(
            method = "startAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void swingOnAttack(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }

    @Redirect(
            method = "continueAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void swingOnBreakingBlock(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.BREAKING_BLOCK, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }

    @Redirect(
            method = "handleKeybinds",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void swingDropItemFromHotbar(LocalPlayer player, InteractionHand hand) {
        if (!EventFactory.onArmSwing(ArmSwingEvent.ArmSwingContext.DROP_ITEM_FROM_HOTBAR, hand)
                .isCanceled()) {
            player.swing(hand);
        }
    }
}
