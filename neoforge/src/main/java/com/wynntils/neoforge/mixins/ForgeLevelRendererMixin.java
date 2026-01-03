/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.wynntils.mc.extension.EntityRenderStateExtension;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class ForgeLevelRendererMixin {
    // This reverts the patch made by NeoForge here: https://github.com/neoforged/NeoForge/pull/858
    // Wynncraft uses this behaviour to hide the local player in certain cases such as the character selection screen.
    @Redirect(
            method =
                    "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/LevelRenderState;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removePlayerFromVisibleEntities(
            List<Object> list,
            Object obj,
            Camera camera,
            Frustum frustum,
            DeltaTracker deltaTracker,
            LevelRenderState renderState) {
        EntityRenderState state = (EntityRenderState) obj;
        Entity entity = ((EntityRenderStateExtension) state).getEntity();

        if (entity instanceof LocalPlayer local && camera.entity() != local) {
            return false;
        }

        return list.add(obj);
    }
}
