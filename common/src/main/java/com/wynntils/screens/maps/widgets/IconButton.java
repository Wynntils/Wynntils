/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconButton extends AbstractWidget {
    private final boolean selected;
    private final int iconRenderX;
    private final int iconRenderY;
    private final int iconWidth;
    private final int iconHeight;
    private final Texture mapIcon;

    public IconButton(int x, int y, int width, Texture mapIcon, boolean selected) {
        super(x, y, width, 20, Component.literal("Icon Button"));

        this.mapIcon = mapIcon;
        this.selected = selected;

        // Scale the icon to fill 80% of the button
        float scaleFactor = 0.8f * Math.min(width, height) / Math.max(mapIcon.width(), mapIcon.height());
        iconWidth = (int) (mapIcon.width() * scaleFactor);
        iconHeight = (int) (mapIcon.height() * scaleFactor);

        // Calculate x/y position of the icon to keep it centered
        iconRenderX = (int) ((x + width / 2f) - iconWidth / 2f);
        iconRenderY = (int) ((y + height / 2f) - iconHeight / 2f);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), width, height);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                mapIcon,
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                mapIcon.width(),
                mapIcon.height());

        if (selected) {
            RenderUtils.drawRect(guiGraphics, CommonColors.LIGHT_BLUE.withAlpha(35), getX(), getY(), width, height);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (McUtils.screen() instanceof PoiCreationScreen poiCreationScreen) {
            poiCreationScreen.setSelectedIcon(mapIcon);
        }

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
