/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;

public class PlayerNametagRenderEvent extends EntityNameTagRenderEvent {
    public PlayerNametagRenderEvent(
            PlayerRenderState renderState,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            EntityRenderDispatcher entityRenderDispatcher,
            Font font) {
        super(renderState, displayName, poseStack, buffer, packedLight, entityRenderDispatcher, font, 0f);
    }

    @Override
    public void setBackgroundOpacity(float backgroundOpacity) {
        // Cannot set background opacity for PlayerNametagRenderEvent. Use EntityNameTagRenderEvent instead.
        // For custom rendered player nametags, CustomNameTagRendererFeature takes care of the background.
    }
}
