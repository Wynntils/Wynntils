/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"))
    private void renderSlotPre(
            int x, int y, float ticks, Player player, ItemStack itemStack, int i, CallbackInfo info) {
        EventFactory.onHotbarSlotRenderPre(itemStack, x, y);
    }

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void renderSlotCountPre(
            int x, int y, float ticks, Player player, ItemStack itemStack, int i, CallbackInfo info) {
        EventFactory.onHotbarSlotRenderCountPre(itemStack, x, y);
    }

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"))
    private void renderSlotPost(
            int x, int y, float ticks, Player player, ItemStack itemStack, int i, CallbackInfo info) {
        EventFactory.onHotbarSlotRenderPost(itemStack, x, y);
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPre(poseStack, partialTick, this.minecraft.getWindow());
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPost(poseStack, partialTick, this.minecraft.getWindow());
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @WrapOperation(
            method = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int onRenderFood(Gui instance, LivingEntity entity, Operation<Integer> original) {
        RenderEvent.Pre event = EventFactory.onRenderFoodPre(new PoseStack(), this.minecraft.getWindow());

        // Return a non-zero value to cancel rendering
        if (event.isCanceled()) return 1;

        return original.call(instance, entity);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderGuiPre(PoseStack poseStack, CallbackInfo ci) {
        if (EventFactory.onRenderCrosshairPre(poseStack, this.minecraft.getWindow())
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void onRenderHeartsPre(
            PoseStack poseStack,
            Player player,
            int x,
            int y,
            int height,
            int i,
            float f,
            int j,
            int k,
            int l,
            boolean bl,
            CallbackInfo ci) {
        if (EventFactory.onRenderHearthsPre(poseStack, this.minecraft.getWindow())
                .isCanceled()) {
            ci.cancel();
        }
    }
}
