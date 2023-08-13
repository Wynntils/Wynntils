/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class PlayerMiniMapPoi extends PlayerPoiBase {
    public PlayerMiniMapPoi(HadesUser user) {
        super(
                user,
                Managers.Feature.getFeatureInstance(MinimapFeature.class)
                        .minimapOverlay
                        .remotePlayersHeadScale
                        .get());
    }

    @Override
    public void renderAt(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f, 0); // center the player icon

        // outline
        BufferedRenderUtils.drawRectBorders(
                poseStack,
                bufferSource,
                user.getRelationColor(),
                renderX,
                renderY,
                renderX + playerHeadRenderSize,
                renderY + playerHeadRenderSize,
                0,
                2);

        // head
        ResourceLocation skin = SkinUtils.getSkin(user.getUuid());
        BufferedRenderUtils.drawTexturedRect(
                poseStack,
                bufferSource,
                skin,
                renderX,
                renderY,
                0,
                playerHeadRenderSize,
                playerHeadRenderSize,
                8,
                8,
                8,
                8,
                64,
                64);

        poseStack.popPose();
    }
}
