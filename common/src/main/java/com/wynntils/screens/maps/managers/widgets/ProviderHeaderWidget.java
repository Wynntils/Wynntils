/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderHeaderWidget extends AbstractWidget {
    private static final int WIDGET_HEIGHT = 26;
    private static final int HEADER_HEIGHT = 16;
    private static final int MIN_HEADER_WIDTH = 100;
    private static final int HEADER_LINE_GAP = 5;
    private static final int TEXT_OFFSET = 10;
    private static final int TEXT_END_PADDING = 20;

    public ProviderHeaderWidget(Component label) {
        super(0, 0, 0, WIDGET_HEIGHT, label);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float headerY = getY() + (this.height - HEADER_HEIGHT) / 2f;

        int textWidth = FontRenderer.getInstance().getFont().width(getMessage());
        int headerWidth = Math.max(MIN_HEADER_WIDTH, TEXT_OFFSET + textWidth + TEXT_END_PADDING);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_SCROLL_BAR_HEADER, getX(), headerY, headerWidth, HEADER_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(getMessage()),
                        getX() + TEXT_OFFSET,
                        getY() + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_HEADER_LINE,
                getX() + headerWidth + HEADER_LINE_GAP,
                headerY,
                this.width - headerWidth - HEADER_LINE_GAP,
                HEADER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
