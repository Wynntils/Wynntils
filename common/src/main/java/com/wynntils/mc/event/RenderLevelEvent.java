/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.neoforged.bus.api.Event;
import org.joml.Matrix4f;

// Note: Neither of these events provide a PoseStack, as it'd be just an empty stack.
public abstract class RenderLevelEvent extends Event {
    private final Matrix4f projectionMatrix;
    private final CameraRenderState cameraRenderState;

    protected RenderLevelEvent(Matrix4f projectionMatrix, CameraRenderState cameraRenderState) {
        this.projectionMatrix = projectionMatrix;
        this.cameraRenderState = cameraRenderState;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }

    public static class Pre extends RenderLevelEvent {
        public Pre(Matrix4f projectionMatrix, CameraRenderState cameraRenderState) {
            super(projectionMatrix, cameraRenderState);
        }
    }

    public static class Post extends RenderLevelEvent {
        public Post(Matrix4f projectionMatrix, CameraRenderState cameraRenderState) {
            super(projectionMatrix, cameraRenderState);
        }
    }
}
