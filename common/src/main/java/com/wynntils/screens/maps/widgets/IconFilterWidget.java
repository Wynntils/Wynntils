/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.maps.IconFilterScreen;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconFilterWidget extends AbstractWidget {
    private final boolean included;
    private final float iconHeight;
    private final float iconRenderX;
    private final float iconRenderY;
    private final float iconWidth;
    private final List<Component> tooltip;
    private final IconFilterScreen filterScreen;
    private final Texture icon;

    public IconFilterWidget(
            int x, int y, int width, int height, Texture icon, IconFilterScreen filterScreen, boolean included) {
        super(x, y, width, height, Component.literal("Icon Filter Widget"));
        this.icon = icon;
        this.filterScreen = filterScreen;
        this.included = included;

        // Scale the icon to fill half of the widget
        float scaleFactor = 0.5f * Math.min(width, height) / Math.max(icon.width(), icon.height());
        iconWidth = icon.width() * scaleFactor;
        iconHeight = icon.height() * scaleFactor;

        // Calculate x/y position of the icon to keep it centered
        iconRenderX = (x + width / 2f) - iconWidth / 2f;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;

        tooltip = included
                ? List.of(Component.translatable(
                        "screens.wynntils.iconFilter.filterExclude.tooltip", EnumUtils.toNiceString(icon)))
                : List.of(Component.translatable(
                        "screens.wynntils.iconFilter.filterInclude.tooltip", EnumUtils.toNiceString(icon)));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), width, height);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                icon.identifier(),
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                icon.width(),
                icon.height());

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        // Highlight to show inclusion
        if (included) {
            RenderUtils.drawRect(guiGraphics, CommonColors.RED.withAlpha(35), getX(), getY(), width, height);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;
        // Toggle if the icon is now included or excluded
        filterScreen.toggleIcon(icon);

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
