/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import com.wynntils.core.components.Services;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Options;loadSelectedResourcePacks(Lnet/minecraft/server/packs/repository/PackRepository;)V",
                            shift = At.Shift.AFTER))
    private void onInitialResourcePackLoad(CallbackInfo ci) {
        // Too early to post events here, but Service components are initialized (and their storages loaded)
        // We add the resource pack to the selected list here
        Services.ResourcePack.preloadResourcePack();
        // Explicitly do not trigger a reload here, as it's too early, and the game will do it later
    }
}
