/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.bus.api.Event;

public class SubmitCustomGeometryEvent extends Event {
    private final LevelRenderState levelRenderState;
    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;
    private final CameraRenderState cameraRenderState;

    public SubmitCustomGeometryEvent(
            LevelRenderState levelRenderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState) {
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.cameraRenderState = cameraRenderState;
    }

    public LevelRenderState getLevelRenderState() {
        return this.levelRenderState;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }
}
