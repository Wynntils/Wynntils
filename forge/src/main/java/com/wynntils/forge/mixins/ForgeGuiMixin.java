/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("HEAD"))
    private void onRenderGuiPre(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("RETURN"))
    private void onRenderGuiPost(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        MixinHelper.post(new RenderEvent.Post(
                guiGraphics, partialTick, this.minecraft.getWindow(), RenderEvent.ElementType.GUI));
    }

    @Inject(
            method = "renderFood(IILnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void onRenderFoodPre(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event =
                new RenderEvent.Pre(guiGraphics, 0, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    // The render food mixin above does not get called when riding a horse, we need this as a replacement.
    @Inject(
            method = "renderHealthMount(IILnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void onRenderHealthMountPre(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event =
                new RenderEvent.Pre(guiGraphics, 0, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
