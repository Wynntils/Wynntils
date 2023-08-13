/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.RenderEvent;
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

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method =
                    "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"))
    private void renderSlotPre(
            PoseStack poseStack,
            int x,
            int y,
            float ticks,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        MixinHelper.post(new HotbarSlotRenderEvent.Pre(poseStack, itemStack, x, y));
    }

    @Inject(
            method =
                    "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void renderSlotCountPre(
            PoseStack poseStack,
            int x,
            int y,
            float ticks,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        MixinHelper.post(new HotbarSlotRenderEvent.CountPre(poseStack, itemStack, x, y));
    }

    @Inject(
            method =
                    "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"))
    private void renderSlotPost(
            PoseStack poseStack,
            int x,
            int y,
            float ticks,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        MixinHelper.post(new HotbarSlotRenderEvent.Post(poseStack, itemStack, x, y));
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"))
    private void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(poseStack, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("RETURN"))
    private void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Post(poseStack, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    // This does not work on Forge. See ForgeGuiMixin for replacement.
    @WrapOperation(
            method = "renderPlayerHealth(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int onRenderFood(Gui instance, LivingEntity entity, Operation<Integer> original) {
        if (!MixinHelper.onWynncraft()) return original.call(instance, entity);

        RenderEvent.Pre event =
                new RenderEvent.Pre(new PoseStack(), 0, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        // we have to reset shader texture
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

        // Return a non-zero value to cancel rendering
        if (event.isCanceled()) return 1;

        return original.call(instance, entity);
    }

    @Inject(
            method = "renderVehicleHealth(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderVehicleHealth(PoseStack poseStack, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        // On Wynncraft we always cancel vehicle health; it has no purpose and it interfers
        // with our foodbar event above
        ci.cancel();
    }

    @Inject(method = "renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderGuiPre(PoseStack poseStack, CallbackInfo ci) {
        RenderEvent.Pre event =
                new RenderEvent.Pre(poseStack, 0, this.minecraft.getWindow(), RenderEvent.ElementType.CROSSHAIR);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "renderHearts(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V",
            at = @At("HEAD"),
            cancellable = true)
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
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event =
                new RenderEvent.Pre(poseStack, 0, this.minecraft.getWindow(), RenderEvent.ElementType.HEALTH_BAR);
        MixinHelper.post(event);

        // we have to reset shader texture
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
