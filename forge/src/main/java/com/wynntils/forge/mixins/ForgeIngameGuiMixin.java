/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.RenderChatEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiMixin extends Gui {
    protected ForgeIngameGuiMixin(Minecraft arg) {
        super(arg);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderGuiPre(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPre(poseStack, partialTick, this.minecraft.getWindow());
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderGuiPost(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        EventFactory.onRenderGuiPost(poseStack, partialTick, this.minecraft.getWindow());
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderFoodPre(int width, int height, PoseStack poseStack, CallbackInfo ci) {
        if (EventFactory.onRenderFoodPre(poseStack, this.minecraft.getWindow()).isCanceled()) {
            ci.cancel();
        }
    }

    // The render food mixin above does not get called when riding a horse, we need this as a replacement.
    @Inject(method = "renderHealthMount", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderHealthMountPre(int width, int height, PoseStack poseStack, CallbackInfo ci) {
        if (EventFactory.onRenderFoodPre(poseStack, this.minecraft.getWindow()).isCanceled()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"))
    private void onRenderChatPre(ChatComponent instance, PoseStack poseStack, int tickCount) {
        RenderChatEvent event = EventFactory.onRenderChatPre(poseStack, this.minecraft.getWindow(), this.chat);
        if (event.isCanceled()) {
            return;
        }

        // This is either this.chat or modified by the event, we render it either way.
        event.getRenderedChat().render(poseStack, this.tickCount);
    }
}
