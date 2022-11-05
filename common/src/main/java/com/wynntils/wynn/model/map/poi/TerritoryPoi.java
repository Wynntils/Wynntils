/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.webapi.profiles.TerritoryProfile;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.model.GuildAttackTimerModel;

public class TerritoryPoi implements Poi {
    private final TerritoryProfile territoryProfile;
    private final MapLocation territoryCenter;
    private final int width;
    private final int height;

    public TerritoryPoi(TerritoryProfile territoryProfile) {
        this.territoryProfile = territoryProfile;
        this.width = territoryProfile.getEndX() - territoryProfile.getStartX();
        this.height = territoryProfile.getEndZ() - territoryProfile.getStartZ();
        this.territoryCenter =
                new MapLocation(territoryProfile.getStartX() + width / 2, 0, territoryProfile.getStartZ() + height / 2);
    }

    @Override
    public MapLocation getLocation() {
        return territoryCenter;
    }

    @Override
    public boolean hasStaticLocation() {
        return true;
    }

    @Override
    public void renderAt(
            PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale, float mapZoom) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);

        final float renderWidth = width * mapZoom;
        final float renderHeight = height * mapZoom;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderZ - renderHeight / 2f;

        final CustomColor guildColor = territoryProfile.getGuildColor();

        RenderUtils.drawRect(
                poseStack, guildColor.withAlpha(65), actualRenderX, actualRenderZ, 0, renderWidth, renderHeight);
        RenderUtils.drawRectBorders(
                poseStack,
                guildColor,
                actualRenderX,
                actualRenderZ,
                actualRenderX + renderWidth,
                actualRenderZ + renderHeight,
                0,
                1.5f);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        territoryProfile.getGuildPrefix(),
                        actualRenderX,
                        actualRenderX + renderWidth,
                        actualRenderZ,
                        actualRenderZ + renderHeight,
                        0,
                        guildColor,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.OUTLINE);

        GuildAttackTimerModel.getAttackTimerForTerritory(territoryProfile.getFriendlyName())
                .ifPresent(attackTimer -> {
                    final String timeLeft = attackTimer.timeUntil();

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
}
