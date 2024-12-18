/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.LightmapEvent;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Shadow
    @Final
    private TextureTarget target;

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void updateLightmap(float partialTicks, CallbackInfo ci) {
        final LightmapEvent lightmapEvent = new LightmapEvent();
        WynntilsMod.postEvent(lightmapEvent);

        if (lightmapEvent.isCanceled()) {
            this.target.clear();
            ci.cancel();
        }
    }
}
