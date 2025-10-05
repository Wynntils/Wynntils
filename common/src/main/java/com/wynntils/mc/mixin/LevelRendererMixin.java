/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    public SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Unique
    private DeltaTracker lastDeltaTracker;

    @Inject(
            at = @At("TAIL"),
            method =
                    "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V")
    private void renderLevelPost(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Matrix4f cullingProjectionMatrix,
            GpuBufferSlice shaderFog,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci) {
        // No PoseStack is provided here, as it'd be just an empty stack.
        MixinHelper.post(
                new RenderLevelEvent.Post(this.minecraft.levelRenderer, deltaTracker, projectionMatrix, camera));
    }

    @Inject(
            at = @At("HEAD"),
            method =
                    "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V")
    private void renderLevelPre(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Matrix4f cullingProjectionMatrix,
            GpuBufferSlice shaderFog,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci) {
        MixinHelper.post(
                new RenderLevelEvent.Pre(this.minecraft.levelRenderer, deltaTracker, projectionMatrix, camera));
    }

    @Inject(
            method =
                    "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
            at = @At("HEAD"))
    private void captureDeltaTracker(
            GraphicsResourceAllocator graphicsResourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Matrix4f cullingProjectionMatrix,
            GpuBufferSlice shaderFog,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci) {
        this.lastDeltaTracker = deltaTracker;
    }

    @Inject(
            method = "method_62214", // framepass.executes lambda inside the addMainPass method
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                            ordinal = 1))
    private void renderTilePost(
            GpuBufferSlice shaderFog,
            LevelRenderState renderState,
            ProfilerFiller profiler,
            Matrix4f frustumMatrix,
            ResourceHandle<RenderTarget> mainResourceHandle,
            ResourceHandle<RenderTarget> translucentResourceHandle,
            boolean renderBlockOutline,
            ResourceHandle<RenderTarget> itemEntityResourceHandle,
            ResourceHandle<RenderTarget> entityOutlineResourceHandle,
            CallbackInfo ci,
            @Local PoseStack poseStack) {
        MixinHelper.post(new RenderTileLevelLastEvent(
                (LevelRenderer) (Object) this,
                poseStack,
                this.submitNodeStorage,
                this.lastDeltaTracker,
                this.levelRenderState.cameraRenderState));
    }

    @ModifyExpressionValue(
            method =
                    "renderEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;Ljava/util/List;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int modifyOutlineColor(int original, @Local Entity entity) {
        EntityExtension entityExt = (EntityExtension) entity;

        if (entityExt.getGlowColor() != CustomColor.NONE) {
            return entityExt.getGlowColorInt();
        }

        return original;
    }

    @Inject(
            at = @At("HEAD"),
            method =
                    "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            cancellable = true)
    private void renderEntity(
            Entity entity,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci) {
        if (!((EntityExtension) entity).isRendered()) {
            ci.cancel();
        }
    }
}
