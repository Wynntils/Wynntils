/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.KeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void keyPressPre(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        KeyInputEvent event = new KeyInputEvent(key, scanCode, action, modifiers);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
