/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.bus.api.Event;
import org.joml.Matrix4f;

public abstract class RenderLevelEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final DeltaTracker deltaTracker;
    private final Matrix4f projectionMatrix;
    private final Camera camera;

    protected RenderLevelEvent(
            LevelRenderer levelRenderer, DeltaTracker deltaTracker, Matrix4f projectionMatrix, Camera camera) {
        this.levelRenderer = levelRenderer;
        this.deltaTracker = deltaTracker;
        this.projectionMatrix = projectionMatrix;
        this.camera = camera;
    }

    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    public DeltaTracker getDeltaTracker() {
        return this.deltaTracker;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Camera getCamera() {
        return camera;
    }

    // Note: This Pre event does not have a poseStack attached.
    // If needed, it can be done, but it is not used in the code base at the moment,
    // so I would rather not hurt mod compatibility by adding it.
    public static class Pre extends RenderLevelEvent {
        public Pre(LevelRenderer levelRenderer, DeltaTracker deltaTracker, Matrix4f projectionMatrix, Camera camera) {
            super(levelRenderer, deltaTracker, projectionMatrix, camera);
        }
    }

    public static class Post extends RenderLevelEvent {
        private final PoseStack poseStack;

        public Post(
                LevelRenderer levelRenderer,
                PoseStack poseStack,
                DeltaTracker deltaTracker,
                Matrix4f projectionMatrix,
                Camera camera) {
            super(levelRenderer, deltaTracker, projectionMatrix, camera);
            this.poseStack = poseStack;
        }

        public PoseStack getPoseStack() {
            return this.poseStack;
        }
    }
}
