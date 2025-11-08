/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.WaypointCreationScreen;
import com.wynntils.screens.maps.WaypointManagementScreen;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class IconButton extends AbstractWidget {
    private final float iconRenderX;
    private final float iconRenderY;
    private final float iconWidth;
    private final float iconHeight;
    private final MapIcon mapIcon;

    private boolean selected;

    public IconButton(int x, int y, int width, MapIcon mapIcon, boolean selected) {
        super(x, y, width, 20, Component.literal("Icon Button"));

        this.mapIcon = mapIcon;
        this.selected = selected;

        // Scale the icon to fill 80% of the button
        float scaleFactor = 0.8f * Math.min(width, height) / Math.max(mapIcon.getWidth(), mapIcon.getHeight());
        iconWidth = mapIcon.getWidth() * scaleFactor;
        iconHeight = mapIcon.getHeight() * scaleFactor;

        // Calculate x/y position of the icon to keep it centered
        iconRenderX = (x + width / 2f) - iconWidth / 2f;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    public IconButton(int x, int y, int width, boolean selected) {
        super(x, y, width, 20, Component.literal("No Icon Button"));

        this.mapIcon = null;
        this.selected = selected;

        iconWidth = -1;
        iconHeight = -1;
        iconRenderX = -1;
        iconRenderY = -1;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        if (mapIcon != null) {
            RenderUtils.drawScalingTexturedRect(
                    poseStack,
                    mapIcon.getResourceLocation(),
                    iconRenderX,
                    iconRenderY,
                    1,
                    iconWidth,
                    iconHeight,
                    mapIcon.getWidth(),
                    mapIcon.getHeight());
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString("No Icon"),
                            getX(),
                            getX() + width,
                            getY(),
                            getY() + height,
                            width - 4,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE,
                            0.5f);
        }

        if (selected) {
            RenderUtils.drawRect(poseStack, CommonColors.LIGHT_BLUE.withAlpha(35), getX(), getY(), 1, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (McUtils.screen() instanceof WaypointCreationScreen waypointCreationScreen) {
            waypointCreationScreen.setSelectedIcon(mapIcon);
        } else if (McUtils.screen() instanceof WaypointManagementScreen waypointManagementScreen) {
            this.selected = !selected;

            String id = mapIcon == null ? MapIcon.NO_ICON_ID : mapIcon.getIconId();
            waypointManagementScreen.toggleIcon(id, selected);
        }

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
