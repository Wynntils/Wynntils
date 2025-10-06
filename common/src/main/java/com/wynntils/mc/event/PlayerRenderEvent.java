/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public final class PlayerRenderEvent extends BaseEvent {
    private final PlayerRenderState playerRenderState;
    private final PoseStack poseStack;
    private final MultiBufferSource buffer;
    private final int packedLight;

    public PlayerRenderEvent(
            PlayerRenderState playerRenderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.playerRenderState = playerRenderState;
        this.poseStack = poseStack;
        this.buffer = buffer;
        this.packedLight = packedLight;
    }

    public PlayerRenderState getPlayerRenderState() {
        return playerRenderState;
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
