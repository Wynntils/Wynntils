/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
    private void renderSlotPre(int x, int y, float ticks, Player player, ItemStack stack, int i, CallbackInfo info) {
        EventFactory.onHotbarSlotRenderPre(stack, x, y);
    }

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"))
    private void renderSlotPost(int x, int y, float ticks, Player player, ItemStack stack, int i, CallbackInfo info) {
        EventFactory.onHotbarSlotRenderPost(stack, x, y);
    }

    // This does not work on Forge. See ForgeIngameGuiMixin for replacement.
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPre(poseStack, partialTick, this.minecraft.getWindow());
    }

    // This does not work on Forge. See ForgeIngameGuiMixin for replacement.
    @Inject(method = "render", at = @At(value = "RETURN"))
    public void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPost(poseStack, partialTick, this.minecraft.getWindow());
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderGuiPre(PoseStack poseStack, CallbackInfo ci) {
        if (EventFactory.onRenderCrosshairPre(poseStack, this.minecraft.getWindow())
                .isCanceled()) {
            ci.cancel();
        }
    }
}
