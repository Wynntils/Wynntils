/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class FabricGuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method =
                    "renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onRenderHotbarAndDecorationsPre(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.HOTBAR));
    }
}
