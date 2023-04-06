/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText2;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.type.DisplayPriority;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

public class TerritoryPoi implements Poi {
    private final TerritoryProfile territoryProfile;
    private final PoiLocation territoryCenter;
    private final int width;
    private final int height;

    private final TerritoryInfo territoryInfo;

    public TerritoryPoi(TerritoryProfile territoryProfile) {
        this(territoryProfile, null);
    }

    public TerritoryPoi(TerritoryProfile territoryProfile, TerritoryInfo territoryInfo) {
        this.territoryProfile = territoryProfile;
        this.width = territoryProfile.getEndX() - territoryProfile.getStartX();
        this.height = territoryProfile.getEndZ() - territoryProfile.getStartZ();
        this.territoryCenter = new PoiLocation(
                territoryProfile.getStartX() + width / 2, null, territoryProfile.getStartZ() + height / 2);

        this.territoryInfo = territoryInfo;
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
        poseStack.translate(0, 0, 100);

        final float renderWidth = width * mapZoom;
        final float renderHeight = height * mapZoom;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderY - renderHeight / 2f;

        CustomColor color;
        if (territoryInfo != null
                && McUtils.mc().screen instanceof GuildMapScreen guildMapScreen
                && guildMapScreen.isResourceMode()) {
            color = territoryInfo.getColor();
        } else {
            color = territoryProfile.getGuildColor();
        }

        BufferedRenderUtils.drawRect(
                poseStack,
                bufferSource,
                color.withAlpha(65),
                actualRenderX,
                actualRenderZ,
                0,
                renderWidth,
                renderHeight);
        BufferedRenderUtils.drawRectBorders(
                poseStack,
                bufferSource,
                color,
                actualRenderX,
                actualRenderZ,
                actualRenderX + renderWidth,
                actualRenderZ + renderHeight,
                0,
                1.5f);

        if (territoryInfo != null && territoryInfo.isHeadquarters()) {
            BufferedRenderUtils.drawTexturedRect(
                    poseStack,
                    bufferSource,
                    Texture.GUILD_HEADQUARTERS_ICON,
                    actualRenderX + renderWidth / 2f - Texture.GUILD_HEADQUARTERS_ICON.width() / 2f,
                    actualRenderZ + renderHeight / 2f - Texture.GUILD_HEADQUARTERS_ICON.height() / 2f);
        } else {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            StyledText2.of(territoryProfile.getGuildPrefix()),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE);
        }

        Models.GuildAttackTimer.getAttackTimerForTerritory(territoryProfile.getFriendlyName())
                .ifPresent(attackTimer -> {
                    final String timeLeft = attackTimer.timerString();

                    BufferedFontRenderer.getInstance()
                            .renderAlignedTextInBox(
                                    poseStack,
                                    bufferSource,
                                    StyledText2.of(timeLeft),
                                    actualRenderX,
                                    actualRenderX + renderWidth,
                                    actualRenderZ,
                                    actualRenderZ + renderHeight,
                                    0,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.OUTLINE);
                });

        if (hovered) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            StyledText2.of(territoryProfile.getFriendlyName()),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        poseStack.popPose();
    }

    @Override
    public PoiLocation getLocation() {
        return territoryCenter;
    }

    @Override
    public boolean hasStaticLocation() {
        return true;
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (width * mapZoom);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (height * mapZoom);
    }

    @Override
    public String getName() {
        return territoryProfile.getName();
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.HIGHEST;
    }

    public TerritoryInfo getTerritoryInfo() {
        return territoryInfo;
    }

    public TerritoryProfile getTerritoryProfile() {
        return territoryProfile;
    }
}
