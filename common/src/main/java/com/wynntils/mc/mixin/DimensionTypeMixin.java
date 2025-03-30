/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.DimensionAmbientLightEvent;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    @Inject(method = "ambientLight()F", at = @At("HEAD"), cancellable = true)
    private void getDimensionAmbientLight(CallbackInfoReturnable<Float> cir) {
        DimensionAmbientLightEvent dimensionLightEvent = new DimensionAmbientLightEvent();
        MixinHelper.post(dimensionLightEvent);

        if (dimensionLightEvent.isCanceled()) {
            cir.setReturnValue(1.0F);
            cir.cancel();
        }
    }
}
