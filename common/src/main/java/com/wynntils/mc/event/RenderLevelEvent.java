/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.eventbus.api.Event;
import org.joml.Matrix4f;

public abstract class RenderLevelEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final float partialTick;
    private final Matrix4f projectionMatrix;
    private final long startNanos;
    private final Camera camera;

    protected RenderLevelEvent(
            LevelRenderer levelRenderer,
            PoseStack poseStack,
            float partialTick,
            Matrix4f projectionMatrix,
            long startNanos,
            Camera camera) {
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.partialTick = partialTick;
        this.projectionMatrix = projectionMatrix;
        this.startNanos = startNanos;
        this.camera = camera;
    }

    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public long getStartNanos() {
        return this.startNanos;
    }

    public Camera getCamera() {
        return camera;
    }

    public static class Pre extends RenderLevelEvent {
        public Pre(
                LevelRenderer levelRenderer,
                PoseStack poseStack,
                float partialTick,
                Matrix4f projectionMatrix,
                long startNanos,
                Camera camera) {
            super(levelRenderer, poseStack, partialTick, projectionMatrix, startNanos, camera);
        }
    }

    public static class Post extends RenderLevelEvent {
        public Post(
                LevelRenderer levelRenderer,
                PoseStack poseStack,
                float partialTick,
                Matrix4f projectionMatrix,
                long startNanos,
                Camera camera) {
            super(levelRenderer, poseStack, partialTick, projectionMatrix, startNanos, camera);
        }
    }
}
