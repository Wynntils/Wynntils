/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

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
            GuiGraphics guiGraphics,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float zoomRenderScale,
            float zoomLevel,
            boolean showLabels) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f); // center the player
        // icon

        // outline
        RenderUtils.drawRectBorders(
                guiGraphics,
                user.getRelationColor(),
                renderX,
                renderY,
                renderX + playerHeadRenderSize,
                renderY + playerHeadRenderSize,
                2);

        // head
        Identifier skin = SkinUtils.getSkin(user.getUuid());
        RenderUtils.drawTexturedRect(
                guiGraphics, skin, renderX, renderY, playerHeadRenderSize, playerHeadRenderSize, 8, 8, 8, 8, 64, 64);

        guiGraphics.pose().popMatrix();
    }
}
