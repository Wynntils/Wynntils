/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.FirstPersonHandRenderEvent;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
            method = "renderItemInHand(FZLorg/joml/Matrix4f;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void renderItemInHandPre(float partialTick, boolean sleeping, Matrix4f projectionMatrix, CallbackInfo ci) {
        FirstPersonHandRenderEvent event = new FirstPersonHandRenderEvent();
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
