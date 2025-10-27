/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HealthTexture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class PlayerMainMapPoi extends PlayerPoiBase {
    public PlayerMainMapPoi(HadesUser user) {
        super(user, 1f);
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

        Identifier skin = SkinUtils.getSkin(user.getUuid());

        // head
        RenderUtils.drawTexturedRect(
                guiGraphics, skin, renderX, renderY, playerHeadRenderSize, playerHeadRenderSize, 8, 8, 8, 8, 64, 64);

        // hat
        RenderUtils.drawTexturedRect(
                guiGraphics, skin, renderX, renderY, playerHeadRenderSize, playerHeadRenderSize, 40, 8, 8, 8, 64, 64);

        // health
        HealthTexture healthTexture = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .remotePlayerHealthTexture
                .get();
        RenderUtils.drawProgressBar(
                guiGraphics,
                Texture.HEALTH_BAR,
                renderX - 10,
                renderY + playerHeadRenderSize + 1,
                renderX + playerHeadRenderSize + 10,
                renderY + playerHeadRenderSize + 7,
                0,
                healthTexture.getTextureY1(),
                81,
                healthTexture.getTextureY2(),
                (float) user.getHealth().getProgress());

        // name
        Font font = FontRenderer.getInstance().getFont();
        int width = font.width(user.getName());
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(user.getName()),
                        renderX - (width - playerHeadRenderSize) / 2f,
                        renderY + playerHeadRenderSize + 8,
                        user.getRelationColor(),
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                .remotePlayerNameShadow
                                .get(),
                        1f);

        guiGraphics.pose().popMatrix();
    }
}
