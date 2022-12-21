/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.map.MapFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.utils.PlayerInfoUtils;
import com.wynntils.core.net.hades.objects.HadesUser;
import com.wynntils.wynn.objects.HealthTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public class PlayerMainMapPoi extends PlayerPoiBase {
    public PlayerMainMapPoi(HadesUser user) {
        super(user, 1f);
    }

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f, 0); // center the player icon

        ResourceLocation skin = PlayerInfoUtils.getSkin(user.getUuid());

        // head
        RenderUtils.drawTexturedRect(
                poseStack, skin, renderX, renderZ, 0, playerHeadRenderSize, playerHeadRenderSize, 8, 8, 8, 8, 64, 64);

        // hat
        RenderUtils.drawTexturedRect(
                poseStack, skin, renderX, renderZ, 1, playerHeadRenderSize, playerHeadRenderSize, 40, 8, 8, 8, 64, 64);

        // health
        HealthTexture healthTexture = MapFeature.INSTANCE.remotePlayerHealthTexture;
        RenderUtils.drawProgressBar(
                poseStack,
                Texture.HEALTH_BAR,
                renderX - 10,
                renderZ + playerHeadRenderSize + 1,
                renderX + playerHeadRenderSize + 10,
                renderZ + playerHeadRenderSize + 7,
                0,
                healthTexture.getTextureY1(),
                81,
                healthTexture.getTextureY2(),
                (float) user.getHealth() / user.getMaxHealth());

        // name
        Font font = FontRenderer.getInstance().getFont();
        int width = font.width(user.getName());
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        user.getName(),
                        renderX - (width - playerHeadRenderSize) / 2f,
                        renderZ + playerHeadRenderSize + 8,
                        user.getRelationColor(),
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        MapFeature.INSTANCE.remotePlayerNameShadow);

        poseStack.popPose();
    }
}
