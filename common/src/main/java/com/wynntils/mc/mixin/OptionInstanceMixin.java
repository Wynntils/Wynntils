/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionInstance.class)
@SuppressWarnings({"rawtypes"})
public abstract class OptionInstanceMixin {
    @Shadow
    private OptionInstance.ValueSet<Double> values;

    /**
     * In Minecraft versions 1.19+, double options are capped between 0 and 1. To prevent this, we replace the value set with our own.
     * */
    @Inject(
            method =
                    "<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;Lnet/minecraft/client/OptionInstance$ValueSet;Lcom/mojang/serialization/Codec;Ljava/lang/Object;Ljava/util/function/Consumer;)V",
            at = @At("RETURN"))
    public void onInit(
            String string,
            OptionInstance.TooltipSupplier tooltipSupplier,
            OptionInstance.CaptionBasedToString captionBasedToString,
            OptionInstance.ValueSet valueSet,
            Codec codec,
            Object object,
            Consumer consumer,
            CallbackInfo ci) {
        if (!string.equals("options.gamma")) return;

        this.values = com.wynntils.mc.objects.UncappedUnitDouble.INSTANCE;
    }
}
