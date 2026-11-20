/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractRenderStatePre(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        RenderEvent.Pre renderEvent =
                new RenderEvent.Pre(guiGraphics, McUtils.window(), RenderElementType.PLAYER_TAB_LIST);
        MixinHelper.post(renderEvent);

        if (renderEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
