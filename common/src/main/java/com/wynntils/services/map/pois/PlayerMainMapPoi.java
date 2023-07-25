/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MapFeature;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HealthTexture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class PlayerMainMapPoi extends PlayerPoiBase {
    public PlayerMainMapPoi(HadesUser user) {
        super(user, 1f);
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

        ResourceLocation skin = SkinUtils.getSkin(user.getUuid());

        // head
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

        // hat
        BufferedRenderUtils.drawTexturedRect(
                poseStack,
                bufferSource,
                skin,
                renderX,
                renderY,
                1,
                playerHeadRenderSize,
                playerHeadRenderSize,
                40,
                8,
                8,
                8,
                64,
                64);

        // health
        HealthTexture healthTexture = Managers.Feature.getFeatureInstance(MapFeature.class)
                .remotePlayerHealthTexture
                .get();
        BufferedRenderUtils.drawProgressBar(
                poseStack,
                bufferSource,
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
        BufferedFontRenderer.getInstance()
                .renderText(
                        poseStack,
                        bufferSource,
                        StyledText.fromString(user.getName()),
                        renderX - (width - playerHeadRenderSize) / 2f,
                        renderY + playerHeadRenderSize + 8,
                        user.getRelationColor(),
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        Managers.Feature.getFeatureInstance(MapFeature.class)
                                .remotePlayerNameShadow
                                .get(),
                        1f);

        poseStack.popPose();
    }
}
