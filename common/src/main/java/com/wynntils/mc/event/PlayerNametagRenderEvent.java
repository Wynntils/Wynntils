/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

public class PlayerNametagRenderEvent extends EntityNameTagRenderEvent {
    public PlayerNametagRenderEvent(
            AvatarRenderState avatarRenderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState) {
        super(avatarRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }
}
