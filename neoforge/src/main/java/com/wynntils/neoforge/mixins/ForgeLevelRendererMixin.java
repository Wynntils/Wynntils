/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ForgeLevelRendererMixin {
    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                            ordinal = 2),
            method = "lambda$addMainPass$2")
    private static void renderTilePost(
            FogParameters fogParameters,
            DeltaTracker deltaTracker,
            Camera camera,
            ProfilerFiller profiler,
            Matrix4f viewMatrix,
            Matrix4f projectionMatrix,
            ResourceHandle<RenderTarget> mainResourceHandle,
            ResourceHandle<RenderTarget> translucentResourceHandle,
            ResourceHandle<RenderTarget> itemEntityResourceHandle,
            ResourceHandle<RenderTarget> weatherResourceHandle,
            Frustum frustum,
            boolean renderBlockOutline,
            ResourceHandle<RenderTarget> entityOutlineResourceHandle,
            CallbackInfo ci,
            @Local PoseStack poseStack) {
        MixinHelper.post(new RenderTileLevelLastEvent(
                McUtils.mc().levelRenderer, poseStack, deltaTracker, projectionMatrix, camera));
    }

    // This reverts the patch made by NeoForge here: https://github.com/neoforged/NeoForge/pull/858
    // Wynncraft uses this behaviour to hide the local player in certain cases such as the character selection screen.
    @Redirect(
            method =
                    "collectVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;)Z",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean removePlayerFromVisibleEntities(
            List<Entity> list, Object obj, Camera camera, Frustum frustum, List<Entity> output) {
        Entity entity = (Entity) obj;
        if (entity instanceof LocalPlayer local && camera.getEntity() != local) {
            return false;
        }

        return list.add((Entity) obj);
    }
}
