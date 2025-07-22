/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.SideListWidget;
import com.wynntils.screens.maps.CustomWaypointIconScreen;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class CustomIconWidget extends SideListWidget {
    private static final String ICON_ID_PREFIX = "wynntils:icon:personal:";

    private final Button removeButton;
    private final CustomWaypointIconScreen waypointIconScreen;
    private final float iconRenderX;
    private final float iconWidth;
    private final float iconHeight;
    private final MapIconImpl customIcon;
    private final String iconId;

    private float iconRenderY;

    public CustomIconWidget(
            int y, int width, int height, MapIconImpl customIcon, CustomWaypointIconScreen waypointIconScreen) {
        super(y, width, height);

        this.customIcon = customIcon;
        this.waypointIconScreen = waypointIconScreen;
        this.iconId = customIcon.getIconId().substring(ICON_ID_PREFIX.length());

        // Scale the icon to fill 80% of the widget height
        float scaleFactor = 0.8f * height / Math.max(customIcon.getWidth(), customIcon.getHeight());
        iconWidth = customIcon.getWidth() * scaleFactor;
        iconHeight = customIcon.getHeight() * scaleFactor;

        // Calculate x/y position of the icon to keep it centered at the start of the widget
        iconRenderX = iconWidth / 2;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;

        this.removeButton = new Button.Builder(
                        Component.literal("🗑"), (button -> waypointIconScreen.removeIcon(customIcon)))
                .pos(width - 20, getY())
                .size(20, height)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(iconId),
                        getX() + 15 + iconWidth,
                        getY() + getHeight() / 2f,
                        getWidth() - 40 - iconWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                customIcon.getResourceLocation(),
                iconRenderX,
                iconRenderY,
                1,
                iconWidth,
                iconHeight,
                customIcon.getWidth(),
                customIcon.getHeight());

        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (removeButton.isMouseOver(mouseX, mouseY)) {
            return removeButton.mouseClicked(mouseX, mouseY, button);
        }

        waypointIconScreen.selectIcon(customIcon);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        removeButton.setY(y);
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }
}
