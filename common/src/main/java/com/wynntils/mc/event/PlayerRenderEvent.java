/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.neoforged.bus.api.Event;

public class PlayerRenderEvent extends Event {
    private final AvatarRenderState avatarRenderState;
    private final PoseStack poseStack;
    private final MultiBufferSource buffer;
    private final int packedLight;

    public PlayerRenderEvent(
            AvatarRenderState avatarRenderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.avatarRenderState = avatarRenderState;
        this.poseStack = poseStack;
        this.buffer = buffer;
        this.packedLight = packedLight;
    }

    public AvatarRenderState getAvatarRenderState() {
        return avatarRenderState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getBuffer() {
        return buffer;
    }

    public int getPackedLight() {
        return packedLight;
    }
}
