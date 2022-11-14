/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPre(poseStack, partialTick, this.minecraft.getWindow());
    }

    // This does not work on Forge. See ForgeIngameGuiMixin for replacement.
    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPost(poseStack, partialTick, this.minecraft.getWindow());
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

        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION); // we have to reset shader texture
    }

    // This doesn't work on forge. See ForgeIngameGuiMixin for replacement.
    // As getVehicleMaxHearts is a private method and is only used by two methods, we can safely override it.
    // This is strange, but it is still better than redirecting...
    // NOTE: This mixin depends on the fact that we always cancel `renderVehicleHealth` with `onVehicleHealthRender`. If
    // we remove that, this mixin will be called twice, making the event be posted twice in 1 render.
    @Inject(method = "getVehicleMaxHearts", at = @At("HEAD"), cancellable = true)
    private void onRenderFoodPre(LivingEntity mountEntity, CallbackInfoReturnable<Integer> cir) {
        RenderEvent.Pre event = EventFactory.onRenderFoodPre(new PoseStack(), this.minecraft.getWindow());

        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION); // we have to reset shader texture

        if (event.isCanceled()) {
            cir.setReturnValue(1);
            cir.cancel();
        }
    }

    // On fabric/quilt, we can just cancel this. Wynncraft does not use vehicle health in any meaningful way.
    // This does not work on forge. See ForgeIngameGui for replacement.
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void onVehicleHealthRender(PoseStack poseStack, CallbackInfo ci) {
        if (WynnUtils.onWorld()) {
            ci.cancel();
        }
    }
}
