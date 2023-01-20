/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.net.hades.objects.HadesUser;
import com.wynntils.features.user.map.MinimapFeature;
import com.wynntils.gui.render.buffered.BufferedRenderUtils;
import com.wynntils.mc.utils.PlayerInfoUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class PlayerMiniMapPoi extends PlayerPoiBase {
    public PlayerMiniMapPoi(HadesUser user) {
        super(user, MinimapFeature.INSTANCE.minimapOverlay.remotePlayersHeadScale);
    }

    @Override
    public void renderAt(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
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
        ResourceLocation skin = PlayerInfoUtils.getSkin(user.getUuid());
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
