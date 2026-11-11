/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.favorites;

import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public final class FavoriteRenderer {
    public static final int SLOT_SIZE = 16;
    // The size of the icon when it is shown on its own, like in lists
    private static final int STANDALONE_ICON_SIZE = 9;

    private FavoriteRenderer() {}

    public static void renderBackground(GuiGraphics guiGraphics, FavoriteCategory category, int x, int y) {
        if (isVisible(category.getBackgroundColor())) {
            RenderUtils.drawRect(guiGraphics, category.getBackgroundColor(), x, y, SLOT_SIZE, SLOT_SIZE);
        }
    }

    public static void renderForeground(GuiGraphics guiGraphics, FavoriteCategory category, int x, int y) {
        if (isVisible(category.getOverlayColor())) {
            RenderUtils.drawRect(guiGraphics, category.getOverlayColor(), x, y, SLOT_SIZE, SLOT_SIZE);
        }

        CustomColor borderColor = category.getBorderColor();
        if (isVisible(borderColor)) {
            RenderUtils.drawRect(guiGraphics, borderColor, x, y, SLOT_SIZE, 1);
            RenderUtils.drawRect(guiGraphics, borderColor, x, y + SLOT_SIZE - 1, SLOT_SIZE, 1);
            RenderUtils.drawRect(guiGraphics, borderColor, x, y + 1, 1, SLOT_SIZE - 2);
            RenderUtils.drawRect(guiGraphics, borderColor, x + SLOT_SIZE - 1, y + 1, 1, SLOT_SIZE - 2);
        }

        renderIcon(guiGraphics, category, x + category.getIconX(), y + category.getIconY(), category.getIconSize());
    }

    public static void renderIcon(GuiGraphics guiGraphics, FavoriteCategory category, int x, int y) {
        renderIcon(guiGraphics, category, x, y, STANDALONE_ICON_SIZE);
    }

    private static void renderIcon(GuiGraphics guiGraphics, FavoriteCategory category, int x, int y, int size) {
        if (!isVisible(category.getIconColor())) return;

        RenderUtils.drawScalingTexturedRect(
                guiGraphics, category.getIcon().getTexture(), category.getIconColor(), x, y, size, size);
    }

    private static boolean isVisible(CustomColor color) {
        return color.a() > 0;
    }
}
