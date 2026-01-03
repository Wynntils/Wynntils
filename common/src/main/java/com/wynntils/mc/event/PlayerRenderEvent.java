/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.neoforged.bus.api.Event;

public class PlayerRenderEvent extends Event {
    private final AvatarRenderState avatarRenderState;
    private final PoseStack poseStack;
    private final SubmitNodeCollector submitNodeCollector;
    private final CameraRenderState cameraRenderState;

    public PlayerRenderEvent(
            AvatarRenderState avatarRenderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState) {
        this.avatarRenderState = avatarRenderState;
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.cameraRenderState = cameraRenderState;
    }

    public AvatarRenderState getAvatarRenderState() {
        return avatarRenderState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public SubmitNodeCollector getSubmitNodeCollector() {
        return submitNodeCollector;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }
}
