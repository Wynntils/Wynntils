/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.emotewheel.widgets;

import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;

public class EmoteSearchWidget extends SearchWidget {
    private static final CustomColor BORDER_COLOR = new CustomColor(80, 53, 45, 255);
    private static final CustomColor FILL_COLOR = new CustomColor(94, 72, 55, 255);

    public EmoteSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawRect(guiGraphics, FILL_COLOR, this.getX(), this.getY(), this.width, this.height);
        RenderUtils.drawRectBorders(
                guiGraphics,
                isHovered ? CommonColors.LIGHT_GRAY : BORDER_COLOR,
                this.getX(),
                this.getY(),
                this.getX() + this.width,
                this.getY() + this.height,
                1f);
    }
}
