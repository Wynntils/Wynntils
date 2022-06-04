/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Final
    @Shadow
    private List<GuiEventListener> children;

    @Final
    @Shadow
    private List<NarratableEntry> narratables;

    @Final
    @Shadow
    private List<Widget> renderables;

    // Making this public is required for the mixin, use this with caution anywhere else
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        renderables.add(widget);
        return addWidget(widget);
    }

    // Making this public is required for the mixin, use this with caution anywhere else
    public <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        children.add(listener);
        narratables.add(listener);
        return listener;
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void initPost(Minecraft client, int width, int height, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        EventFactory.onScreenCreated(screen, this::addRenderableWidget);
    }

    @Inject(
            method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            cancellable = true)
    private void renderTooltipPre(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
        ItemTooltipRenderEvent.Pre e = EventFactory.onItemTooltipRenderPre(poseStack, itemStack, mouseX, mouseY);
        if (e.isCanceled()) ci.cancel();
    }

    @Inject(
            method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN"))
    private void renderTooltipPost(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
        EventFactory.onItemTooltipRenderPost(poseStack, itemStack, mouseX, mouseY);
    }
}
