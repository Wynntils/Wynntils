/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.KeyMappingEvent;
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

    @WrapOperation(
            method = "keyPress",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"))
    private void onKeyMappingSet(InputConstants.Key key, boolean held, Operation<Void> original) {
        KeyMappingEvent event =
                new KeyMappingEvent(key, held ? KeyMappingEvent.Operation.SET : KeyMappingEvent.Operation.UNSET);
        MixinHelper.post(event);

        if (!event.isCanceled()) {
            original.call(key, held);
        }
    }

    @WrapOperation(
            method = "keyPress",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
    private void onKeyMappingClick(InputConstants.Key key, Operation<Void> original) {
        KeyMappingEvent event = new KeyMappingEvent(key, KeyMappingEvent.Operation.CLICK);
        MixinHelper.post(event);

        if (!event.isCanceled()) {
            original.call(key);
        }
    }
}
