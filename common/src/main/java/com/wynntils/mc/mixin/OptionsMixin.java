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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
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
    private void wrapProcessOptions(Options instance, @Coerce Object fieldAccess, Operation<Void> original) {
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
    private Object wrapLegacyKeybindFieldAccess(Object fieldAccess) {
        if (!(fieldAccess instanceof OptionsLoadVisitorAccessor loadVisitorAccessor)) {
            return fieldAccess;
        }

        CompoundTag options = loadVisitorAccessor.wynntils$getOptions();
        Map<String, String> legacyAliases = KeyBindManager.getLegacyKeybindAliases(options);
        if (legacyAliases.isEmpty()) {
            return fieldAccess;
        }

        return Proxy.newProxyInstance(
                fieldAccess.getClass().getClassLoader(),
                fieldAccess.getClass().getInterfaces(),
                (proxy, method, args) -> processFieldAccessInvocation(fieldAccess, legacyAliases, method, args));
    }

    @Unique
    private Object processFieldAccessInvocation(
            Object fieldAccess, Map<String, String> legacyAliases, Method method, Object[] args) throws Throwable {
        if ("process".equals(method.getName())
                && args != null
                && args.length == 2
                && args[0] instanceof String optionKey
                && args[1] instanceof String) {
            String legacyValue = legacyAliases.get(optionKey);
            if (legacyValue != null) {
                wynntils$legacyKeybindsMigrated = true;
                return legacyValue;
            }
        }

        try {
            return method.invoke(fieldAccess, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
