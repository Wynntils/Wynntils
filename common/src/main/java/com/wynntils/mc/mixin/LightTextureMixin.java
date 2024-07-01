/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LightmapEvent;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Redirect(
            method = "updateLightTexture",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    private void updateLightmapRGB(NativeImage image, int x, int y, int rgb) {
        final LightmapEvent lightmapEvent = new LightmapEvent(rgb);
        MixinHelper.post(lightmapEvent);
        image.setPixelRGBA(x, y, lightmapEvent.getRgb());
    }
}
