/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.widgets.categorymanager.OptionsScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.AbstractOptionWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class PathOptionsScrollBarWidget extends OptionsScrollBarWidget {
    public PathOptionsScrollBarWidget(int x, int y, int width, int height, Component title) {
        super(x, y, width, height);
        setMessage(title);
    }

    @Override
    public List<AbstractOptionWidget<?>> getWidgets() {
        return getRegisteredWidgets().stream().filter(widget -> widget.display).toList();
    }

    @Override
    protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_WIDGET_BACKGROUND, getX(), getY() - 8, getWidth(), getHeight() + 8);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int x = getX() + 10;
        int y = getY() - 15;
        RenderUtils.drawNineSliceScalingTexturedRect(graphics, Texture.MANAGER_SCROLL_BAR_HEADER, x, y, 150, 16);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(getMessage()),
                        x + 20,
                        y + 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        if (!isInsideViewport(mouseX, mouseY)) return List.of();

        return getWidgets().stream()
                .filter(widget -> widget.visible && widget.isMouseOver(mouseX, mouseY))
                .findFirst()
                .map(widget -> widget.getTooltipLines(mouseX, mouseY))
                .orElse(List.of());
    }
}
