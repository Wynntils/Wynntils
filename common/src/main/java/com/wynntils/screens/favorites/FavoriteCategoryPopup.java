/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.favorites;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.services.favorites.FavoriteRenderer;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public final class FavoriteCategoryPopup {
    private static final int ROW_HEIGHT = 12;
    private static final int PADDING = 2;
    private static final int ICON_SPACE = 12;
    private static final CustomColor BACKGROUND_COLOR = new CustomColor(0, 0, 0, 220);
    private static final CustomColor HOVERED_COLOR = new CustomColor(255, 255, 255, 60);

    private final List<FavoriteCategory> categories;
    private final FavoriteCategory currentCategory;
    private final String noCategoryText;
    private final Consumer<FavoriteCategory> onPick;
    private final int x;
    private final int y;
    private final int width;

    public FavoriteCategoryPopup(
            int x, int y, FavoriteCategory currentCategory, String noCategoryText, Consumer<FavoriteCategory> onPick) {
        this.categories = Services.Favorites.getCategories();
        this.currentCategory = currentCategory;
        this.noCategoryText = noCategoryText;
        this.onPick = onPick;

        int textWidth = McUtils.mc().font.width(noCategoryText);
        for (FavoriteCategory category : categories) {
            textWidth = Math.max(textWidth, McUtils.mc().font.width(category.getName()));
        }
        this.width = ICON_SPACE + textWidth + 2 * PADDING;

        this.x = Math.min(x, McUtils.screen().width - width);
        this.y = Math.min(y, McUtils.screen().height - getHeight());
    }

    public static FavoriteCategoryPopup forItem(String itemName, int x, int y) {
        return new FavoriteCategoryPopup(
                x,
                y,
                Services.Favorites.getCategory(itemName),
                I18n.get("screens.wynntils.lootFilters.removeFavorite"),
                category -> Services.Favorites.setCategory(itemName, category));
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RenderUtils.drawRect(guiGraphics, BACKGROUND_COLOR, x, y, width, getHeight());

        int hoveredRow = getRow(mouseX, mouseY);

        for (int row = 0; row <= categories.size(); row++) {
            int rowY = y + PADDING + row * ROW_HEIGHT;
            if (row == hoveredRow) {
                RenderUtils.drawRect(guiGraphics, HOVERED_COLOR, x, rowY, width, ROW_HEIGHT);
            }

            boolean isNoCategoryRow = row == categories.size();
            if (!isNoCategoryRow) {
                FavoriteRenderer.renderIcon(guiGraphics, categories.get(row), x + PADDING, rowY + 1);
            }

            String text = isNoCategoryRow ? noCategoryText : categories.get(row).getName();
            CustomColor textColor = isNoCategoryRow
                    ? CommonColors.RED
                    : categories.get(row) == currentCategory ? CommonColors.YELLOW : CommonColors.WHITE;
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(text),
                            x + PADDING + ICON_SPACE,
                            rowY + 2,
                            textColor,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);
        }
    }

    public void mouseClicked(double mouseX, double mouseY) {
        int row = getRow(mouseX, mouseY);
        if (row == -1) return;

        onPick.accept(row == categories.size() ? null : categories.get(row));
    }

    private int getRow(double mouseX, double mouseY) {
        if (mouseX < x || mouseX >= x + width) return -1;

        int row = (int) Math.floor((mouseY - y - PADDING) / ROW_HEIGHT);
        return row >= 0 && row <= categories.size() ? row : -1;
    }

    private int getHeight() {
        return (categories.size() + 1) * ROW_HEIGHT + 2 * PADDING;
    }
}
