/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.neoforged.bus.api.Event;

public class RenderTileLevelLastEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final SubmitNodeStorage submitNodeStorage;
    private final DeltaTracker deltaTracker;
    private final CameraRenderState cameraRenderState;

    public RenderTileLevelLastEvent(
            LevelRenderer levelRenderer,
            PoseStack poseStack,
            SubmitNodeStorage submitNodeStorage,
            DeltaTracker deltaTracker,
            CameraRenderState cameraRenderState) {
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.submitNodeStorage = submitNodeStorage;
        this.deltaTracker = deltaTracker;
        this.cameraRenderState = cameraRenderState;
    }

    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public SubmitNodeStorage getSubmitNodeStorage() {
        return submitNodeStorage;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }
}
