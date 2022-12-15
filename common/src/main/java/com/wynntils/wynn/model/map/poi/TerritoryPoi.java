/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.maps.GuildMapScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.territory.objects.TerritoryInfo;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;

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
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {

        poseStack.pushPose();
        poseStack.translate(0, 0, 100);

        final float renderWidth = width * mapZoom;
        final float renderHeight = height * mapZoom;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderZ - renderHeight / 2f;

        CustomColor color;
        if (territoryInfo != null
                && McUtils.mc().screen instanceof GuildMapScreen guildMapScreen
                && guildMapScreen.isResourceMode()) {
            color = territoryInfo.getColor();
        } else {
            color = territoryProfile.getGuildColor();
        }

        RenderUtils.drawRect(
                poseStack, color.withAlpha(65), actualRenderX, actualRenderZ, 0, renderWidth, renderHeight);
        RenderUtils.drawRectBorders(
                poseStack,
                color,
                actualRenderX,
                actualRenderZ,
                actualRenderX + renderWidth,
                actualRenderZ + renderHeight,
                0,
                1.5f);

        if (territoryInfo != null && territoryInfo.isHeadquarters()) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.GUILD_HEADQUARTERS_ICON,
                    actualRenderX + renderWidth / 2f - Texture.GUILD_HEADQUARTERS_ICON.width() / 2f,
                    actualRenderZ + renderHeight / 2f - Texture.GUILD_HEADQUARTERS_ICON.height() / 2f);
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            territoryProfile.getGuildPrefix(),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            color,
                            HorizontalAlignment.Center,
                            VerticalAlignment.Middle,
                            FontRenderer.TextShadow.OUTLINE);
        }

        Models.GuildAttackTimer.getAttackTimerForTerritory(territoryProfile.getFriendlyName())
                .ifPresent(attackTimer -> {
                    final String timeLeft = attackTimer.timerString();

                    FontRenderer.getInstance()
                            .renderAlignedTextInBox(
                                    poseStack,
                                    timeLeft,
                                    actualRenderX,
                                    actualRenderX + renderWidth,
                                    actualRenderZ,
                                    actualRenderZ + renderHeight,
                                    0,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.Center,
                                    VerticalAlignment.Bottom,
                                    FontRenderer.TextShadow.OUTLINE);
                });

        if (hovered) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            territoryProfile.getFriendlyName(),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.Center,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.OUTLINE);
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
