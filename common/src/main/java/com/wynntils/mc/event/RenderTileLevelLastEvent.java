/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.neoforged.bus.api.Event;

public class RenderTileLevelLastEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final SubmitNodeStorage submitNodeStorage;
    private final DeltaTracker deltaTracker;
    private final Camera camera;

    public RenderTileLevelLastEvent(
            LevelRenderer levelRenderer,
            PoseStack poseStack,
            SubmitNodeStorage submitNodeStorage,
            DeltaTracker deltaTracker,
            Camera camera) {
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.submitNodeStorage = submitNodeStorage;
        this.deltaTracker = deltaTracker;
        this.camera = camera;
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

    public Camera getCamera() {
        return camera;
    }
}
