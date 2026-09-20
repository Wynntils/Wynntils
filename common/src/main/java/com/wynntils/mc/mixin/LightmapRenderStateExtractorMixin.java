/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LightmapBrightnessEvent;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @ModifyExpressionValue(
            method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float overrideBrightness(float brightness) {
        LightmapBrightnessEvent lightmapBrightnessEvent = new LightmapBrightnessEvent(brightness);
        MixinHelper.post(lightmapBrightnessEvent);

        return lightmapBrightnessEvent.getBrightnes();
    }
}
