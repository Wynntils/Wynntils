/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
    //
    // "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
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
