/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.screens.maps.WaypointManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconButton extends AbstractWidget {
    private final float iconRenderX;
    private final float iconRenderY;
    private final float iconWidth;
    private final float iconHeight;
    private final Texture mapIcon;

    private boolean selected;

    public IconButton(int x, int y, int width, Texture mapIcon, boolean selected) {
        super(x, y, width, 20, Component.literal("Icon Button"));

        this.mapIcon = mapIcon;
        this.selected = selected;

        // Scale the icon to fill 80% of the button
        float scaleFactor = 0.8f * Math.min(width, height) / Math.max(mapIcon.width(), mapIcon.height());
        iconWidth = mapIcon.width() * scaleFactor;
        iconHeight = mapIcon.height() * scaleFactor;

        // Calculate x/y position of the icon to keep it centered
        iconRenderX = (x + width / 2f) - iconWidth / 2f;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), width, height);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                mapIcon.identifier(),
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                mapIcon.width(),
                mapIcon.height());

        if (selected) {
            RenderUtils.drawRect(guiGraphics, CommonColors.LIGHT_BLUE.withAlpha(35), getX(), getY(), width, height);
        }

        if (this.isHovered) {
            if (McUtils.screen() instanceof WaypointManagementScreen) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(
                                ComponentUtils.wrapTooltips(
                                        List.of(
                                                Component.translatable(
                                                        "screens.wynntils.waypointManagementGui.iconFilterTooltip1"),
                                                Component.translatable(
                                                        "screens.wynntils.waypointManagementGui.iconFilterTooltip2")),
                                        250),
                                Component::getVisualOrderText),
                        mouseX,
                        mouseY);
            }

            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (McUtils.screen() instanceof PoiCreationScreen poiCreationScreen) {
            poiCreationScreen.setSelectedIcon(mapIcon);
        } else if (McUtils.screen() instanceof WaypointManagementScreen waypointManagementScreen) {
            this.selected = !selected;

            waypointManagementScreen.toggleIcon(mapIcon, selected, KeyboardUtils.isShiftDown());
        }

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
