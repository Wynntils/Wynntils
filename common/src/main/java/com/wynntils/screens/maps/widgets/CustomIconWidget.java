/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.CustomWaypointIconScreen;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CustomIconWidget extends AbstractWidget {
    private static final String ICON_ID_PREFIX = "wynntils:icon:personal:";

    private final CustomWaypointIconScreen waypointIconScreen;
    private final float iconRenderX;
    private final float iconWidth;
    private final float iconHeight;
    private final MapIconImpl customIcon;

    private Button removeButton;
    private float iconRenderY;

    public CustomIconWidget(
            int y, int width, int height, MapIconImpl customIcon, CustomWaypointIconScreen waypointIconScreen) {
        super(0, y, width, height, Component.literal("Custom Icon Widget"));

        this.customIcon = customIcon;
        this.waypointIconScreen = waypointIconScreen;

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
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.BROWN.withAlpha(isHovered ? 150 : 100),
                this.getX(),
                this.getY(),
                width,
                height);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(customIcon.getIconId().substring(ICON_ID_PREFIX.length())),
                        getX() + 15 + iconWidth,
                        getY() + getHeight() / 2f,
                        getWidth() - 40 - iconWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                customIcon.getIdentifier(),
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                customIcon.getWidth(),
                customIcon.getHeight());

        removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (removeButton.isMouseOver(event.x(), event.y())) {
            return removeButton.mouseClicked(event, isDoubleClick);
        }

        waypointIconScreen.selectIcon(customIcon);

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        removeButton.setY(y);
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
