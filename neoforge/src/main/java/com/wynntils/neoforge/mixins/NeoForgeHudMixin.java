/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class NeoForgeHudMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(
            method =
                    "extractHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractHotbarPre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.HOTBAR));
    }

    @Inject(
            method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractSelectedItemNamePre(GuiGraphicsExtractor guiGraphics, int yShift, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, this.minecraft.getWindow(), RenderElementType.SELECTED_ITEM);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
