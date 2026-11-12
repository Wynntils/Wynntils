/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.favorites.widgets;

import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutInputWidget;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;

public class LootFilterInputWidget extends NewLoadoutInputWidget {
    public LootFilterInputWidget(
            int x, int y, int width, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, onUpdateConsumer, textboxScreen);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                isHovered ? Texture.GUIDE_WIDGET_BACKGROUND_HOVERED : Texture.GUIDE_WIDGET_BACKGROUND,
                getX(),
                getY(),
                this.width,
                this.height);
    }
}
