/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.components.Managers;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.mc.mixin.accessors.OptionsLoadVisitorAccessor;
import java.util.Map;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    @Unique
    private boolean wynntils$legacyKeybindsMigrated = false;

    @Inject(method = "load()V", at = @At("HEAD"))
    private void onLoad(CallbackInfo ci) {
        wynntils$legacyKeybindsMigrated = false;
        Managers.KeyBind.registerKeybinds((Options) (Object) this);
    }

    @WrapOperation(
            method = "load()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Options;processOptions(Lnet/minecraft/client/Options$FieldAccess;)V"))
    private void wrapProcessOptions(Options instance, Options.FieldAccess fieldAccess, Operation<Void> original) {
        original.call(instance, wrapLegacyKeybindFieldAccess(fieldAccess));
    }

    @Inject(method = "load()V", at = @At("RETURN"))
    private void onLoadReturn(CallbackInfo ci) {
        if (!wynntils$legacyKeybindsMigrated) {
            return;
        }

        wynntils$legacyKeybindsMigrated = false;
        ((Options) (Object) this).save();
    }

    @Unique
    private Options.FieldAccess wrapLegacyKeybindFieldAccess(Options.FieldAccess fieldAccess) {
        if (!(fieldAccess instanceof OptionsLoadVisitorAccessor loadVisitorAccessor)) {
            return fieldAccess;
        }

        CompoundTag options = loadVisitorAccessor.wynntils$getOptions();
        Map<String, String> legacyAliases = KeyBindManager.getLegacyKeybindAliases(options);
        if (legacyAliases.isEmpty()) {
            return fieldAccess;
        }

        return new LegacyKeybindFieldAccess(fieldAccess, legacyAliases, () -> wynntils$legacyKeybindsMigrated = true);
    }
}
