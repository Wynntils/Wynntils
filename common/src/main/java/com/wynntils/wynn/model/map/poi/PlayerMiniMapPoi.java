/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.map.MinimapFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.mc.utils.PlayerInfoUtils;
import com.wynntils.core.net.hades.objects.HadesUser;
import net.minecraft.resources.ResourceLocation;

public class PlayerMiniMapPoi extends PlayerPoiBase {
    public PlayerMiniMapPoi(HadesUser user) {
        super(user, MinimapFeature.INSTANCE.minimapOverlay.remotePlayersHeadScale);
    }

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f, 0); // center the player icon

        // outline
        RenderUtils.drawRectBorders(
                poseStack,
                user.getRelationColor(),
                renderX,
                renderZ,
                renderX + playerHeadRenderSize,
                renderZ + playerHeadRenderSize,
                0,
                2);

        // head
        ResourceLocation skin = PlayerInfoUtils.getSkin(user.getUuid());
        RenderUtils.drawTexturedRect(
                poseStack, skin, renderX, renderZ, 0, playerHeadRenderSize, playerHeadRenderSize, 8, 8, 8, 8, 64, 64);

        poseStack.popPose();
    }
}
