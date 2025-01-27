/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class FabricLevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                            ordinal = 2),
            method = "method_62214")
    private void renderTilePost(
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
            boolean renderBlockOutline,
            Frustum frustum,
            ResourceHandle<RenderTarget> entityOutlineResourceHandle,
            CallbackInfo ci,
            @Local PoseStack poseStack) {
        MixinHelper.post(new RenderTileLevelLastEvent(
                this.minecraft.levelRenderer, poseStack, deltaTracker, projectionMatrix, camera));
    }
}
