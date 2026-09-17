/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

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
                    "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void submitCustomGeometry(
            LevelRenderState levelRenderState,
            SubmitNodeCollector collector,
            boolean renderOutline,
            CallbackInfo ci,
            @Local PoseStack poseStack) {
        MixinHelper.post(new SubmitCustomGeometryEvent(
                levelRenderState, poseStack, collector, levelRenderState.cameraRenderState));
    }

    @WrapWithCondition(
            method =
                    "submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private boolean onSubmitEntity(
            EntityRenderDispatcher entityRenderDispatcher,
            EntityRenderState renderState,
            CameraRenderState cameraRenderState,
            double camX,
            double camY,
            double camZ,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector) {
        Entity entity = ((EntityRenderStateExtension) renderState).getEntity();

        // Mods that inject into renderstate extraction may mean our entity is null
        if (entity == null) return true;

        return ((EntityExtension) entity).isRendered();
    }
}
