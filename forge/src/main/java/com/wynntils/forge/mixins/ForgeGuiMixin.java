/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin extends Gui {
    protected ForgeGuiMixin(Minecraft arg) {
        super(arg, arg.getItemRenderer());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(poseStack, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Post(poseStack, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderFoodPre(int width, int height, PoseStack poseStack, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event =
                new RenderEvent.Pre(poseStack, 0, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        // we have to reset shader texture
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    // The render food mixin above does not get called when riding a horse, we need this as a replacement.
    @Inject(method = "renderHealthMount", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderHealthMountPre(int width, int height, PoseStack poseStack, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event =
                new RenderEvent.Pre(poseStack, 0, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        // we have to reset shader texture
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
