/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.consumers.atlas.AtlasManager;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.resources.model.AtlasManager.class)
public class AtlasManagerMixin {
    @Shadow
    @Final
    @Mutable
    private static List<net.minecraft.client.resources.model.AtlasManager.AtlasConfig> KNOWN_ATLASES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addAtlas(CallbackInfo ci) {
        List<net.minecraft.client.resources.model.AtlasManager.AtlasConfig> atlases = new ArrayList<>(KNOWN_ATLASES);

        atlases.addAll(AtlasManager.ATLASES);

        KNOWN_ATLASES = List.copyOf(atlases);
    }
}
