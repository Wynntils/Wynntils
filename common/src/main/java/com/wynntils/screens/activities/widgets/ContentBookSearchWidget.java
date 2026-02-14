/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;

public class ContentBookSearchWidget extends SearchWidget {
    public ContentBookSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.CONTENT_BOOK_SEARCH, getX() - 15, getY());
    }
}
