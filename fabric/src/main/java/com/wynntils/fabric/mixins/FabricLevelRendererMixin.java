/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
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

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

//    @Inject(
//            at =
//                    @At(
//                            value = "INVOKE",
//                            target =
//                                    "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
//                            ordinal = 2),
//            method = "method_62214") // framepass.executes lambda inside the addMainPass method
//    private void renderTilePost(
//            GpuBufferSlice shaderFog,
//            DeltaTracker deltaTracker,
//            Camera camera,
//            ProfilerFiller profiler,
//            Matrix4f viewMatrix,
//            ResourceHandle<RenderTarget> mainResourceHandle,
//            ResourceHandle<RenderTarget> translucentResourceHandle,
//            boolean renderBlockOutline,
//            Frustum frustum,
//            ResourceHandle<RenderTarget> itemEntityResourceHandle,
//            ResourceHandle<RenderTarget> entityOutlineResourceHandle,
//            CallbackInfo ci,
//            @Local PoseStack poseStack) {
//        MixinHelper.post(new RenderTileLevelLastEvent(
//                this.minecraft.levelRenderer, poseStack, submitNodeStorage, deltaTracker, camera));
//    }
}
