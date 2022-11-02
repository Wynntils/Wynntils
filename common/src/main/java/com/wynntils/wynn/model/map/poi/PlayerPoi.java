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
import com.wynntils.sockets.objects.HadesUser;
import com.wynntils.wynn.objects.HealthTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public class PlayerPoi implements Poi {
    private static final float PLAYER_HEAD_RENDER_SIZE = 20;

    private final HadesUser user;

    public PlayerPoi(HadesUser user) {
        this.user = user;
    }

    @Override
    public MapLocation getLocation() {
        return user.getMapLocation();
    }

    @Override
    public boolean hasStaticLocation() {
        return false;
    }

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(-PLAYER_HEAD_RENDER_SIZE / 2f, -PLAYER_HEAD_RENDER_SIZE / 2f, 0); // center the player icon

        ResourceLocation skin = PlayerInfoUtils.getSkin(user.getUuid());

        // head
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                renderX,
                renderZ,
                0,
                PLAYER_HEAD_RENDER_SIZE,
                PLAYER_HEAD_RENDER_SIZE,
                8,
                8,
                8,
                8,
                64,
                64);

        // hat
        RenderUtils.drawTexturedRect(
                poseStack,
                skin,
                renderX,
                renderZ,
                1,
                PLAYER_HEAD_RENDER_SIZE,
                PLAYER_HEAD_RENDER_SIZE,
                40,
                8,
                8,
                8,
                64,
                64);

        HealthTexture healthTexture = MapFeature.INSTANCE.remotePlayerHealthTexture;

        RenderUtils.drawProgressBar(
                poseStack,
                Texture.HEALTH_BAR,
                renderX - 10,
                renderZ + PLAYER_HEAD_RENDER_SIZE + 1,
                renderX + PLAYER_HEAD_RENDER_SIZE + 10,
                renderZ + PLAYER_HEAD_RENDER_SIZE + 7,
                0,
                healthTexture.getTextureY1(),
                81,
                healthTexture.getTextureY2(),
                (float) user.getHealth() / user.getMaxHealth());

        Font font = FontRenderer.getInstance().getFont();
        int width = font.width(user.getName());
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        user.getName(),
                        renderX - (width - PLAYER_HEAD_RENDER_SIZE) / 2f,
                        renderZ + PLAYER_HEAD_RENDER_SIZE + 8,
                        user.getRelationColor(),
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        MapFeature.INSTANCE.remotePlayerNameShadow);

        poseStack.popPose();
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (PLAYER_HEAD_RENDER_SIZE + 20);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (PLAYER_HEAD_RENDER_SIZE + 17);
    }

    @Override
    public String getName() {
        return user.getName();
    }
}
