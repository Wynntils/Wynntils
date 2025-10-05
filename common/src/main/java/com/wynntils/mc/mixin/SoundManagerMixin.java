/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.SoundPlayedEvent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {
    @Inject(
            method =
                    "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onSoundPlayed(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        SoundPlayedEvent event = new SoundPlayedEvent(sound);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            cir.cancel();
        }
    }
}
